// services/ShowServices.java
package services;

import database.DBConnection;
import models.RefundItem;
import models.Role;
import models.Show;
import models.ShowStatus;
import repositories.ShowCancellationRepository;
import repositories.ShowRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShowServices {

    public static final int GAP_MINUTES = 30;

    private ShowRepository showRepo = new ShowRepository();
    private ShowCancellationRepository cancelRepo = new ShowCancellationRepository();
    private RefundGateway gateway;

    public ShowServices() {
        this(new MockRefundGateway());
    }

    public ShowServices(RefundGateway gateway) {
        this.gateway = gateway;
    }

    // ---------------- authorization ----------------

    private void checkLoggedIn(Role role) {
        if (role == null) {
            throw new ShowSchedulingException("You must be logged in.");
        }
    }

    private void checkCanManage(Role role) {
        if (role != Role.ADMIN && role != Role.THEATRE_MANAGER) {
            throw new ShowSchedulingException("Access denied: only ADMIN or THEATRE_MANAGER can do this.");
        }
    }

    private void checkCanCancel(Role role) {
        if (role != Role.ADMIN) {
            throw new ShowSchedulingException("Access denied: only ADMIN can cancel a show.");
        }
    }

    // ---------------- validation ----------------

    public LocalDateTime calculateEndTime(LocalDateTime start, int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new ShowSchedulingException("Movie duration must be more than 0 minutes.");
        }
        return start.plusMinutes(durationMinutes);
    }

    public void validatePrice(String seatType, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ShowSchedulingException(seatType + " price must be greater than 0.");
        }
        if (price.scale() > 2) {
            throw new ShowSchedulingException(seatType + " price can have only 2 decimal places.");
        }
    }

    public void validateStartTime(LocalDateTime start, LocalDate releaseDate, LocalDateTime now) {
        if (start == null) {
            throw new ShowSchedulingException("Start date and time are required.");
        }
        if (!start.isAfter(now)) {
            throw new ShowSchedulingException("A show cannot be scheduled in the past.");
        }
        if (releaseDate != null && start.toLocalDate().isBefore(releaseDate)) {
            throw new ShowSchedulingException("Show date is before the movie release date " + releaseDate + ".");
        }
    }

    // true if the new show overlaps an old one or is closer than 30 minutes to it
    public static boolean conflicts(LocalDateTime newStart, LocalDateTime newEnd,
                                    LocalDateTime oldStart, LocalDateTime oldEnd) {
        boolean startsBeforeNewEndsWithGap = oldStart.isBefore(newEnd.plusMinutes(GAP_MINUTES));
        boolean endsAfterNewStartsWithGap = oldEnd.plusMinutes(GAP_MINUTES).isAfter(newStart);
        return startsBeforeNewEndsWithGap && endsAfterNewStartsWithGap;
    }

    public void checkScreenAvailability(Connection conn, int screenId, LocalDateTime start,
                                        LocalDateTime end, int excludeShowId) throws SQLException {
        // look one day before and after so overnight shows are also checked
        List<Show> nearby = showRepo.getScheduledShowsOnScreen(conn, screenId,
                start.minusDays(1), end.plusDays(1), excludeShowId);
        for (Show old : nearby) {
            if (conflicts(start, end, old.getStartTime(), old.getEndTime())) {
                throw new ShowSchedulingException("Screen " + screenId + " is not free. Show "
                        + old.getShowId() + " runs from " + old.getStartTime() + " to " + old.getEndTime()
                        + ". A gap of at least " + GAP_MINUTES + " minutes is needed between shows.");
            }
        }
    }

    // checks everything and returns the end time. Call this inside a transaction.
    public LocalDateTime validateShowSchedule(Connection conn, int movieId, int screenId,
                                              LocalDateTime start, int excludeShowId) throws SQLException {
        int duration = showRepo.getMovieDuration(conn, movieId);
        if (duration == -1) {
            throw new ShowSchedulingException("Movie with ID " + movieId + " does not exist.");
        }
        if (!showRepo.lockScreen(conn, screenId)) {
            throw new ShowSchedulingException("Screen with ID " + screenId + " does not exist.");
        }
        LocalDate releaseDate = showRepo.getMovieReleaseDate(conn, movieId);
        validateStartTime(start, releaseDate, LocalDateTime.now());
        LocalDateTime end = calculateEndTime(start, duration);
        checkScreenAvailability(conn, screenId, start, end, excludeShowId);
        return end;
    }

    // ---------------- create ----------------

    public Show createShow(Role role, int movieId, int screenId, LocalDateTime start,
                           BigDecimal regular, BigDecimal premium, BigDecimal recliner) {
        checkCanManage(role);
        validatePrice("Regular", regular);
        validatePrice("Premium", premium);
        validatePrice("Recliner", recliner);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            LocalDateTime end = validateShowSchedule(conn, movieId, screenId, start, 0);
            Show show = new Show(movieId, screenId, start, end, regular, premium, recliner);
            int id = showRepo.insertShow(conn, show);
            show.setShowId(id);

            conn.commit();
            return show;
        } catch (ShowSchedulingException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    // ---------------- view ----------------

    public List<Show> getAllShows(Role role) {
        return filterShows(role, null, null, null);
    }

    public List<Show> getShowsByMovie(Role role, int movieId) {
        return filterShows(role, movieId, null, null);
    }

    public List<Show> getShowsByScreen(Role role, int screenId) {
        return filterShows(role, null, screenId, null);
    }

    public List<Show> getShowsByDate(Role role, LocalDate date) {
        return filterShows(role, null, null, date);
    }

    public List<Show> filterShows(Role role, Integer movieId, Integer screenId, LocalDate date) {
        checkLoggedIn(role);
        boolean onlyScheduled = (role == Role.CUSTOMER);   // customers only see scheduled shows
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            return showRepo.filterShows(conn, movieId, screenId, date, onlyScheduled);
        } catch (SQLException e) {
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    public List<Show> getUpcomingShows(Role role) {
        checkLoggedIn(role);
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            return showRepo.getUpcomingShows(conn, LocalDateTime.now());
        } catch (SQLException e) {
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    public Show getShowById(Role role, int showId) {
        checkLoggedIn(role);
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Show show = showRepo.getShowById(conn, showId, false);
            if (show == null || (role == Role.CUSTOMER && show.getStatus() != ShowStatus.SCHEDULED)) {
                throw new ShowSchedulingException("Show with ID " + showId + " not found.");
            }
            return show;
        } catch (SQLException e) {
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    public boolean hasExistingBookings(int showId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            return showRepo.countActiveBookings(conn, showId) > 0;
        } catch (SQLException e) {
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    // ---------------- update ----------------

    // newMovieId / newScreenId = 0 and newStart = null mean "keep the old value"
    public Show updateShow(Role role, int showId, int newMovieId, int newScreenId, LocalDateTime newStart) {
        checkCanManage(role);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Show show = showRepo.getShowById(conn, showId, true);
            if (show == null) {
                throw new ShowSchedulingException("Show with ID " + showId + " not found.");
            }
            if (show.getStatus() == ShowStatus.CANCELLED) {
                throw new ShowSchedulingException("A cancelled show cannot be updated.");
            }

            int movieId = (newMovieId != 0) ? newMovieId : show.getMovieId();
            int screenId = (newScreenId != 0) ? newScreenId : show.getScreenId();
            LocalDateTime start = (newStart != null) ? newStart : show.getStartTime();

            boolean changed = movieId != show.getMovieId() || screenId != show.getScreenId()
                    || !start.equals(show.getStartTime());
            if (!changed) {
                conn.rollback();
                return show;
            }

            if (showRepo.countActiveBookings(conn, showId) > 0) {
                throw new ShowSchedulingException("This show already has bookings, so the movie, screen "
                        + "and start time cannot be changed. Only prices can be updated.");
            }

            LocalDateTime end = validateShowSchedule(conn, movieId, screenId, start, showId);
            show.setMovieId(movieId);
            show.setScreenId(screenId);
            show.setStartTime(start);
            show.setEndTime(end);
            showRepo.updateSchedule(conn, show);

            conn.commit();
            return show;
        } catch (ShowSchedulingException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    // allowed even when bookings exist, old bookings keep the price they were booked with
    public Show updateShowPrices(Role role, int showId, BigDecimal regular,
                                 BigDecimal premium, BigDecimal recliner) {
        checkCanManage(role);
        validatePrice("Regular", regular);
        validatePrice("Premium", premium);
        validatePrice("Recliner", recliner);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Show show = showRepo.getShowById(conn, showId, true);
            if (show == null) {
                throw new ShowSchedulingException("Show with ID " + showId + " not found.");
            }
            if (show.getStatus() == ShowStatus.CANCELLED) {
                throw new ShowSchedulingException("Prices of a cancelled show cannot be changed.");
            }

            show.setRegularPrice(regular);
            show.setPremiumPrice(premium);
            show.setReclinerPrice(recliner);
            showRepo.updatePrices(conn, show);

            conn.commit();
            return show;
        } catch (ShowSchedulingException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            throw new ShowSchedulingException("Database error: " + e.getMessage());
        } finally {
            close(conn);
        }
    }

    // ---------------- cancel ----------------

    // Step 1 (one transaction): cancel show and bookings, create refund records.
    // Step 2 (after commit): call the refund gateway and save the result.
    public String cancelShow(Role role, String userName, int showId, String reason) {
        checkCanCancel(role);
        if (reason == null || reason.trim().isEmpty()) {
            throw new ShowSchedulingException("A cancellation reason is required.");
        }

        List<RefundItem> refunds = new ArrayList<>();
        int bookingCount = 0;
        BigDecimal totalRefund = BigDecimal.ZERO;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Show show = showRepo.getShowById(conn, showId, true);
            if (show == null) {
                throw new ShowSchedulingException("Show with ID " + showId + " not found.");
            }
            if (show.getStatus() == ShowStatus.CANCELLED) {
                throw new ShowSchedulingException("Show " + showId + " is already cancelled.");
            }

            showRepo.updateStatus(conn, showId, ShowStatus.CANCELLED);

            List<RefundItem> items = cancelBookingsForShow(conn, showId);
            bookingCount = items.size();

            for (RefundItem item : items) {
                if (item.getPaymentId() != 0) {
                    int refundId = cancelRepo.insertRefund(conn, showId, item);
                    item.setRefundId(refundId);
                    cancelRepo.setPaymentStatus(conn, item.getPaymentId(), "REFUND_PENDING");
                    refunds.add(item);
                    totalRefund = totalRefund.add(item.getAmount());
                }
            }

            cancelRepo.insertAudit(conn, showId, userName, role.name(), reason.trim(),
                    bookingCount, totalRefund);

            conn.commit();
        } catch (ShowSchedulingException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            throw new ShowSchedulingException("Cancellation failed, nothing was changed: " + e.getMessage());
        } finally {
            close(conn);
        }

        int completed = 0;
        int failed = 0;
        for (RefundItem item : refunds) {
            if (processRefund(item)) {
                completed++;
            } else {
                failed++;
            }
        }

        return "Show " + showId + " cancelled. Bookings cancelled: " + bookingCount
                + ", total refund: " + totalRefund
                + ", refunds completed: " + completed + ", refunds failed: " + failed;
    }

    // cancels all active bookings of the show and returns them
    private List<RefundItem> cancelBookingsForShow(Connection conn, int showId) throws SQLException {
        List<RefundItem> items = cancelRepo.getActiveBookings(conn, showId);
        for (RefundItem item : items) {
            cancelRepo.cancelBooking(conn, item.getBookingId());
        }
        return items;
    }

    // calls the gateway and saves COMPLETED or FAILED
    private boolean processRefund(RefundItem item) {
        String reference = null;
        try {
            reference = gateway.refund(item.getPaymentId(), item.getAmount());
        } catch (RuntimeException e) {
            reference = null;
        }
        boolean success = (reference != null);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            cancelRepo.updateRefund(conn, item.getRefundId(), success ? "COMPLETED" : "FAILED",
                    gateway.getName(), reference);
            cancelRepo.setPaymentStatus(conn, item.getPaymentId(), success ? "REFUNDED" : "REFUND_FAILED");
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            return false;
        } finally {
            close(conn);
        }
        return success;
    }

    // ---------------- helpers ----------------

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.out.println("Rollback failed: " + e.getMessage());
            }
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection: " + e.getMessage());
            }
        }
    }
}