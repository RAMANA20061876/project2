// repositories/ShowCancellationRepository.java
package repositories;

import models.RefundItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ShowCancellationRepository {

    // active bookings of a show with their successful payment (paymentId = 0 if no payment)
    public List<RefundItem> getActiveBookings(Connection conn, int showId) throws SQLException {
        String sql = "SELECT b.booking_id, p.payment_id, p.amount FROM bookings b "
                + "LEFT JOIN payments p ON p.booking_id = b.booking_id AND p.status = 'SUCCESS' "
                + "WHERE b.show_id = ? AND b.status IN ('CONFIRMED', 'PENDING') FOR UPDATE";
        List<RefundItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int bookingId = rs.getInt(1);
                    int paymentId = rs.getInt(2);   // 0 when null
                    BigDecimal amount = rs.getBigDecimal(3);
                    items.add(new RefundItem(bookingId, paymentId, amount));
                }
            }
        }
        return items;
    }

    public void cancelBooking(Connection conn, int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    public void setPaymentStatus(Connection conn, int paymentId, String status) throws SQLException {
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        }
    }

    public int insertRefund(Connection conn, int showId, RefundItem item) throws SQLException {
        String sql = "INSERT INTO refunds (show_id, booking_id, payment_id, amount, status) "
                + "VALUES (?, ?, ?, ?, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, showId);
            ps.setInt(2, item.getBookingId());
            ps.setInt(3, item.getPaymentId());
            ps.setBigDecimal(4, item.getAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not get the new refund id");
    }

    public void updateRefund(Connection conn, int refundId, String status,
                             String gatewayName, String gatewayRef) throws SQLException {
        String sql = "UPDATE refunds SET status = ?, gateway_name = ?, gateway_ref = ? WHERE refund_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, gatewayName);
            ps.setString(3, gatewayRef);
            ps.setInt(4, refundId);
            ps.executeUpdate();
        }
    }

    public void insertAudit(Connection conn, int showId, String userName, String role,
                            String reason, int bookingsCancelled, BigDecimal totalRefund) throws SQLException {
        String sql = "INSERT INTO show_cancellations (show_id, cancelled_by, role, reason, "
                + "bookings_cancelled, total_refund) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setString(2, userName);
            ps.setString(3, role);
            ps.setString(4, reason);
            ps.setInt(5, bookingsCancelled);
            ps.setBigDecimal(6, totalRefund);
            ps.executeUpdate();
        }
    }
}