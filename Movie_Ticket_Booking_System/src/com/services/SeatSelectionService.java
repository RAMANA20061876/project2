package com.movieticket.service;

import com.movieticket.model.Movie;
import com.movieticket.model.Seat;
import com.movieticket.model.SeatLock;
import com.movieticket.model.Show;
import com.movieticket.model.User;
import com.movieticket.repository.InMemoryStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles user registration, seat availability and seat locking.
 */
public class SeatSelectionService {

    public static final int MAX_SEATS_PER_BOOKING = 6;
    public static final int LOCK_DURATION_MINUTES = 10;
    public static final int BOOKING_CLOSE_MINUTES = 15;
    public static final int ADULT_AGE = 18;

    public static final String SEAT_AVAILABLE = "AVAILABLE";
    public static final String SEAT_LOCKED = "LOCKED";
    public static final String SEAT_BOOKED = "BOOKED";

    private final InMemoryStorage storage;

    public SeatSelectionService(InMemoryStorage storage) {
        this.storage = storage;
    }

    // ---------------------------------------------------------------
    // User registration
    // ---------------------------------------------------------------
    public User registerUser(String name, String phone, String email, LocalDate dateOfBirth) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        }
        if (email == null || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (dateOfBirth == null || dateOfBirth.isAfter(storage.getCurrentTime().toLocalDate())) {
            throw new IllegalArgumentException("Date of birth is not valid.");
        }
        for (User existing : storage.getAllUsers()) {
            if (existing.getEmail().equalsIgnoreCase(email.trim())) {
                throw new IllegalArgumentException("This email is already registered.");
            }
        }
        User user = new User(storage.nextUserId(), name.trim(), phone.trim(), email.trim(), dateOfBirth);
        storage.saveUser(user);
        return user;
    }

    // ---------------------------------------------------------------
    // Lock expiry
    // ---------------------------------------------------------------
    /** Removes every seat lock that is older than 10 minutes. */
    public synchronized void releaseExpiredLocks() {
        LocalDateTime now = storage.getCurrentTime();
        for (SeatLock lock : storage.getAllLocks()) {   // getAllLocks returns a copy, so removing is safe
            if (lock.isExpired(now)) {
                storage.removeLock(lock.getShowId(), lock.getSeatNumber());
            }
        }
    }

    // ---------------------------------------------------------------
    // Seat availability (always releases expired locks first)
    // ---------------------------------------------------------------
    public synchronized Map<String, String> getSeatStatusMap(String showId) {
        releaseExpiredLocks();
        Show show = getShowOrThrow(showId);
        Map<String, String> result = new LinkedHashMap<>();
        for (Seat seat : show.getAllSeats()) {
            result.put(seat.getSeatNumber(), findStatus(showId, seat));
        }
        return result;
    }

    public synchronized List<String> getAvailableSeats(String showId) {
        List<String> available = new ArrayList<>();
        Map<String, String> statusMap = getSeatStatusMap(showId);
        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            if (entry.getValue().equals(SEAT_AVAILABLE)) {
                available.add(entry.getKey());
            }
        }
        return available;
    }

    private String findStatus(String showId, Seat seat) {
        if (seat.isBooked()) {
            return SEAT_BOOKED;
        }
        if (storage.getLock(showId, seat.getSeatNumber()) != null) {
            return SEAT_LOCKED;
        }
        return SEAT_AVAILABLE;
    }

    // ---------------------------------------------------------------
    // Booking window
    // ---------------------------------------------------------------
    /** Booking closes 15 minutes before the show starts. */
    public boolean isBookingClosed(Show show) {
        LocalDateTime closeTime = show.getStartTime().minusMinutes(BOOKING_CLOSE_MINUTES);
        return !storage.getCurrentTime().isBefore(closeTime);
    }

    // ---------------------------------------------------------------
    // Seat locking
    // ---------------------------------------------------------------
    public synchronized List<SeatLock> lockSeats(String userId, String showId, List<String> requestedSeats) {
        releaseExpiredLocks();

        User user = storage.getUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        Show show = getShowOrThrow(showId);
        if (requestedSeats == null || requestedSeats.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat.");
        }

        LocalDateTime now = storage.getCurrentTime();

        // Rule: booking closes 15 minutes before the show
        if (isBookingClosed(show)) {
            throw new IllegalStateException("Booking is closed for show " + showId
                    + " (it closes " + BOOKING_CLOSE_MINUTES + " minutes before the show starts).");
        }

        // Rule: under 18 cannot book A-certified movies
        Movie movie = show.getMovie();
        if (movie.isAdultOnly() && user.getAgeOn(now.toLocalDate()) < ADULT_AGE) {
            throw new IllegalStateException("Users under " + ADULT_AGE + " cannot book A-certified movie '"
                    + movie.getTitle() + "'.");
        }

        // Clean the seat list (trim, upper case, no duplicates)
        List<String> seats = new ArrayList<>();
        for (String seat : requestedSeats) {
            String clean = seat.trim().toUpperCase();
            if (seats.contains(clean)) {
                throw new IllegalArgumentException("Seat " + clean + " was entered more than once.");
            }
            seats.add(clean);
        }

        // Rule: maximum 6 seats per booking
        int alreadyLocked = countActiveLocks(userId, showId);
        if (alreadyLocked + seats.size() > MAX_SEATS_PER_BOOKING) {
            throw new IllegalStateException("You can select a maximum of " + MAX_SEATS_PER_BOOKING
                    + " seats per booking (you already hold " + alreadyLocked + ").");
        }

        // Check ALL seats first, so we lock either all of them or none
        for (String seatNumber : seats) {
            Seat seat = show.getSeat(seatNumber);
            if (seat == null) {
                throw new IllegalArgumentException("Seat " + seatNumber + " does not exist in show " + showId + ".");
            }
            if (seat.isBooked()) {
                throw new IllegalStateException("Seat " + seatNumber + " is already booked.");
            }
            SeatLock existing = storage.getLock(showId, seatNumber);
            if (existing != null) {
                if (existing.getUserId().equals(userId)) {
                    throw new IllegalStateException("You have already locked seat " + seatNumber + ".");
                }
                throw new IllegalStateException("Seat " + seatNumber + " is locked by another user.");
            }
        }

        // All good - create the locks
        List<SeatLock> newLocks = new ArrayList<>();
        LocalDateTime expiresAt = now.plusMinutes(LOCK_DURATION_MINUTES);
        for (String seatNumber : seats) {
            SeatLock lock = new SeatLock(storage.nextLockId(), showId, seatNumber, userId, now, expiresAt);
            storage.saveLock(lock);
            newLocks.add(lock);
        }
        return newLocks;
    }

    private int countActiveLocks(String userId, String showId) {
        int count = 0;
        for (SeatLock lock : storage.getAllLocks()) {
            if (lock.getUserId().equals(userId) && lock.getShowId().equals(showId)) {
                count++;
            }
        }
        return count;
    }

    private Show getShowOrThrow(String showId) {
        Show show = storage.getShow(showId);
        if (show == null) {
            throw new IllegalArgumentException("Show not found: " + showId);
        }
        return show;
    }
}
