package com.movieticket.service;

import com.movieticket.model.Booking;
import com.movieticket.model.Seat;
import com.movieticket.model.SeatLock;
import com.movieticket.model.Show;
import com.movieticket.model.User;
import com.movieticket.repository.InMemoryStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates bookings from locked seats and handles the (simulated) payment result.
 */
public class BookingService {

    private final InMemoryStorage storage;
    private final SeatSelectionService seatSelectionService;

    public BookingService(InMemoryStorage storage, SeatSelectionService seatSelectionService) {
        this.storage = storage;
        this.seatSelectionService = seatSelectionService;
    }

    /** Creates a PENDING booking using all seats the user has currently locked for this show. */
    public synchronized Booking createBooking(String userId, String showId) {
        seatSelectionService.releaseExpiredLocks();
        refreshAllPendingBookings();

        User user = storage.getUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        Show show = storage.getShow(showId);
        if (show == null) {
            throw new IllegalArgumentException("Show not found: " + showId);
        }
        if (seatSelectionService.isBookingClosed(show)) {
            throw new IllegalStateException("Booking is closed for show " + showId + ".");
        }

        // Collect the seats this user has locked for this show
        List<String> lockedSeats = new ArrayList<>();
        for (SeatLock lock : storage.getAllLocks()) {
            if (lock.getUserId().equals(userId) && lock.getShowId().equals(showId)) {
                lockedSeats.add(lock.getSeatNumber());
            }
        }
        if (lockedSeats.isEmpty()) {
            throw new IllegalStateException("No active seat locks found. Select seats first (locks last "
                    + SeatSelectionService.LOCK_DURATION_MINUTES + " minutes).");
        }

        // Only one pending booking per user per show
        for (Booking existing : storage.getAllBookings()) {
            if (existing.getUserId().equals(userId) && existing.getShowId().equals(showId)
                    && Booking.STATUS_PENDING.equals(existing.getStatus())) {
                throw new IllegalStateException("You already have a pending booking (" + existing.getBookingId()
                        + ") for this show. Complete its payment first.");
            }
        }

        double total = show.getTicketPrice() * lockedSeats.size();
        Booking booking = new Booking(storage.nextBookingId(), userId, showId, lockedSeats, total,
                storage.getCurrentTime());
        storage.saveBooking(booking);
        return booking;
    }

    /**
     * Simulated payment. paymentSuccess = true  -> CONFIRMED, seats become booked.
     *                    paymentSuccess = false -> FAILED, seats are released.
     */
    public synchronized Booking makePayment(String bookingId, boolean paymentSuccess) {
        seatSelectionService.releaseExpiredLocks();

        Booking booking = storage.getBooking(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        if (!Booking.STATUS_PENDING.equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is already " + booking.getStatus() + ". Payment not allowed.");
        }

        // If the 10-minute lock ran out, the booking fails
        refreshBooking(booking);
        if (Booking.STATUS_FAILED.equals(booking.getStatus())) {
            throw new IllegalStateException("Seat lock expired before payment. Booking marked as FAILED.");
        }

        Show show = storage.getShow(booking.getShowId());

        // Safety check: a seat must never be booked twice for the same show
        for (String seatNumber : booking.getSeatNumbers()) {
            Seat seat = show.getSeat(seatNumber);
            if (seat.isBooked()) {
                releaseBookingLocks(booking);
                booking.setStatus(Booking.STATUS_FAILED);
                throw new IllegalStateException("Seat " + seatNumber + " is already booked. Booking marked as FAILED.");
            }
        }

        if (paymentSuccess) {
            for (String seatNumber : booking.getSeatNumbers()) {
                show.getSeat(seatNumber).setBooked(true);
                storage.removeLock(booking.getShowId(), seatNumber);
            }
            booking.setStatus(Booking.STATUS_CONFIRMED);
        } else {
            releaseBookingLocks(booking);
            booking.setStatus(Booking.STATUS_FAILED);
        }
        return booking;
    }

    /** Returns the booking after checking whether its seat locks have expired. */
    public synchronized Booking getBooking(String bookingId) {
        seatSelectionService.releaseExpiredLocks();
        Booking booking = storage.getBooking(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        refreshBooking(booking);
        return booking;
    }

    public synchronized String getBookingStatus(String bookingId) {
        return getBooking(bookingId).getStatus();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void refreshAllPendingBookings() {
        for (Booking booking : storage.getAllBookings()) {
            refreshBooking(booking);
        }
    }

    /** A PENDING booking becomes FAILED if any of its seats is no longer locked by the same user. */
    private void refreshBooking(Booking booking) {
        if (!Booking.STATUS_PENDING.equals(booking.getStatus())) {
            return;
        }
        for (String seatNumber : booking.getSeatNumbers()) {
            SeatLock lock = storage.getLock(booking.getShowId(), seatNumber);
            if (lock == null || !lock.getUserId().equals(booking.getUserId())) {
                releaseBookingLocks(booking);
                booking.setStatus(Booking.STATUS_FAILED);
                return;
            }
        }
    }

    /** Removes only the locks that belong to this booking's user. */
    private void releaseBookingLocks(Booking booking) {
        for (String seatNumber : booking.getSeatNumbers()) {
            SeatLock lock = storage.getLock(booking.getShowId(), seatNumber);
            if (lock != null && lock.getUserId().equals(booking.getUserId())) {
                storage.removeLock(booking.getShowId(), seatNumber);
            }
        }
    }
}
