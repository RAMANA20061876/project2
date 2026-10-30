package com.movieticket.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_FAILED = "FAILED";

    private final String bookingId;
    private final String userId;
    private final String showId;
    private final List<String> seatNumbers;
    private final double totalAmount;
    private final LocalDateTime createdAt;
    private String status;

    public Booking(String bookingId, String userId, String showId, List<String> seatNumbers,
                   double totalAmount, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.showId = showId;
        this.seatNumbers = new ArrayList<>(seatNumbers);
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = STATUS_PENDING;
    }

    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getShowId() { return showId; }
    public List<String> getSeatNumbers() { return new ArrayList<>(seatNumbers); }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return bookingId + " | " + showId + " | " + seatNumbers + " | " + status;
    }
}
