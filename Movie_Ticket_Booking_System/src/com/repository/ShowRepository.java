// repositories/ShowRepository.java
package repositories;

import models.Show;
import models.ShowStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShowRepository {

    private static final String COLUMNS = "show_id, movie_id, screen_id, start_time, end_time, "
            + "regular_price, premium_price, recliner_price, status";

    // returns -1 if the movie does not exist
    public int getMovieDuration(Connection conn, int movieId) throws SQLException {
        String sql = "SELECT duration_minutes FROM movies WHERE movie_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // returns null if there is no release date
    public LocalDate getMovieReleaseDate(Connection conn, int movieId) throws SQLException {
        String sql = "SELECT release_date FROM movies WHERE movie_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getDate(1) != null) {
                    return rs.getDate(1).toLocalDate();
                }
            }
        }
        return null;
    }

    // locks the screen row so two people cannot schedule on the same screen at once
    public boolean lockScreen(Connection conn, int screenId) throws SQLException {
        String sql = "SELECT screen_id FROM screens WHERE screen_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertShow(Connection conn, Show show) throws SQLException {
        String sql = "INSERT INTO shows (movie_id, screen_id, start_time, end_time, "
                + "regular_price, premium_price, recliner_price, status) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, show.getMovieId());
            ps.setInt(2, show.getScreenId());
            ps.setTimestamp(3, Timestamp.valueOf(show.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(show.getEndTime()));
            ps.setBigDecimal(5, show.getRegularPrice());
            ps.setBigDecimal(6, show.getPremiumPrice());
            ps.setBigDecimal(7, show.getReclinerPrice());
            ps.setString(8, show.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not get the new show id");
    }

    public void updateSchedule(Connection conn, Show show) throws SQLException {
        String sql = "UPDATE shows SET movie_id = ?, screen_id = ?, start_time = ?, end_time = ? "
                + "WHERE show_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, show.getMovieId());
            ps.setInt(2, show.getScreenId());
            ps.setTimestamp(3, Timestamp.valueOf(show.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(show.getEndTime()));
            ps.setInt(5, show.getShowId());
            ps.executeUpdate();
        }
    }

    public void updatePrices(Connection conn, Show show) throws SQLException {
        String sql = "UPDATE shows SET regular_price = ?, premium_price = ?, recliner_price = ? "
                + "WHERE show_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, show.getRegularPrice());
            ps.setBigDecimal(2, show.getPremiumPrice());
            ps.setBigDecimal(3, show.getReclinerPrice());
            ps.setInt(4, show.getShowId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection conn, int showId, ShowStatus status) throws SQLException {
        String sql = "UPDATE shows SET status = ? WHERE show_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, showId);
            ps.executeUpdate();
        }
    }

    // returns null if not found
    public Show getShowById(Connection conn, int showId, boolean lock) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM shows WHERE show_id = ?";
        if (lock) {
            sql = sql + " FOR UPDATE";
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShow(rs);
                }
            }
        }
        return null;
    }

    // null for movieId / screenId / date means "no filter"
    public List<Show> filterShows(Connection conn, Integer movieId, Integer screenId,
                                  LocalDate date, boolean onlyScheduled) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM shows WHERE 1=1");
        if (movieId != null) {
            sql.append(" AND movie_id = ?");
        }
        if (screenId != null) {
            sql.append(" AND screen_id = ?");
        }
        if (date != null) {
            sql.append(" AND start_time >= ? AND start_time < ?");
        }
        if (onlyScheduled) {
            sql.append(" AND status = 'SCHEDULED'");
        }
        sql.append(" ORDER BY start_time");

        List<Show> shows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (movieId != null) {
                ps.setInt(i++, movieId);
            }
            if (screenId != null) {
                ps.setInt(i++, screenId);
            }
            if (date != null) {
                ps.setTimestamp(i++, Timestamp.valueOf(date.atStartOfDay()));
                ps.setTimestamp(i++, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapShow(rs));
                }
            }
        }
        return shows;
    }

    public List<Show> getUpcomingShows(Connection conn, LocalDateTime now) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM shows WHERE status = 'SCHEDULED' "
                + "AND start_time > ? ORDER BY start_time";
        List<Show> shows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(now));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapShow(rs));
                }
            }
        }
        return shows;
    }

    // scheduled shows on this screen between from and to (excludeShowId = 0 means exclude nothing)
    public List<Show> getScheduledShowsOnScreen(Connection conn, int screenId, LocalDateTime from,
                                                LocalDateTime to, int excludeShowId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM shows WHERE screen_id = ? AND status = 'SCHEDULED' "
                + "AND end_time > ? AND start_time < ? AND show_id <> ?";
        List<Show> shows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            ps.setInt(4, excludeShowId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapShow(rs));
                }
            }
        }
        return shows;
    }

    public int countActiveBookings(Connection conn, int showId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE show_id = ? "
                + "AND status IN ('CONFIRMED', 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Show mapShow(ResultSet rs) throws SQLException {
        Show show = new Show();
        show.setShowId(rs.getInt("show_id"));
        show.setMovieId(rs.getInt("movie_id"));
        show.setScreenId(rs.getInt("screen_id"));
        show.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        show.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        show.setRegularPrice(rs.getBigDecimal("regular_price"));
        show.setPremiumPrice(rs.getBigDecimal("premium_price"));
        show.setReclinerPrice(rs.getBigDecimal("recliner_price"));
        show.setStatus(ShowStatus.valueOf(rs.getString("status")));
        return show;
    }
}