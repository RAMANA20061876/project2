package com.movieticket.repository;

import com.movieticket.model.Booking;
import com.movieticket.model.Movie;
import com.movieticket.model.SeatLock;
import com.movieticket.model.Show;
import com.movieticket.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Our "database" - just HashMaps in memory. Data is lost when the program stops.
 */
public class InMemoryStorage {

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Movie> movies = new LinkedHashMap<>();
    private final Map<String, Show> shows = new LinkedHashMap<>();
    private final Map<String, SeatLock> seatLocks = new HashMap<>();   // key = showId_seatNumber
    private final Map<String, Booking> bookings = new LinkedHashMap<>();

    private int userCounter = 0;
    private int lockCounter = 0;
    private int bookingCounter = 0;

    // Extra minutes added to the real clock. Only used to TEST lock expiry without waiting 10 minutes.
    private long clockOffsetMinutes = 0;

    // ---------------- Clock ----------------
    public LocalDateTime getCurrentTime() {
        return LocalDateTime.now().plusMinutes(clockOffsetMinutes);
    }

    public void advanceClock(long minutes) {
        clockOffsetMinutes += minutes;
    }

    // ---------------- ID generators ----------------
    public String nextUserId() { return "U" + (++userCounter); }
    public String nextLockId() { return "L" + (++lockCounter); }
    public String nextBookingId() { return "B" + (++bookingCounter); }

    // ---------------- Users ----------------
    public void saveUser(User user) { users.put(user.getUserId(), user); }
    public User getUser(String userId) { return users.get(userId); }
    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }

    // ---------------- Movies ----------------
    public void saveMovie(Movie movie) { movies.put(movie.getMovieId(), movie); }
    public Movie getMovie(String movieId) { return movies.get(movieId); }
    public List<Movie> getAllMovies() { return new ArrayList<>(movies.values()); }

    // ---------------- Shows ----------------
    public void saveShow(Show show) { shows.put(show.getShowId(), show); }
    public Show getShow(String showId) { return shows.get(showId); }
    public List<Show> getAllShows() { return new ArrayList<>(shows.values()); }

    // ---------------- Seat locks ----------------
    private String lockKey(String showId, String seatNumber) {
        return showId + "_" + seatNumber;
    }

    public void saveLock(SeatLock lock) {
        seatLocks.put(lockKey(lock.getShowId(), lock.getSeatNumber()), lock);
    }

    public SeatLock getLock(String showId, String seatNumber) {
        return seatLocks.get(lockKey(showId, seatNumber));
    }

    public void removeLock(String showId, String seatNumber) {
        seatLocks.remove(lockKey(showId, seatNumber));
    }

    /** Returns a COPY, so callers can remove locks while looping. */
    public List<SeatLock> getAllLocks() { return new ArrayList<>(seatLocks.values()); }

    // ---------------- Bookings ----------------
    public void saveBooking(Booking booking) { bookings.put(booking.getBookingId(), booking); }
    public Booking getBooking(String bookingId) { return bookings.get(bookingId); }
    public List<Booking> getAllBookings() { return new ArrayList<>(bookings.values()); }
}
