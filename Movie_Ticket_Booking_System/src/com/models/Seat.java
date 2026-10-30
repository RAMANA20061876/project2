package com.movieticket.model;

/**
 * One seat of ONE show. Because every Show creates its own Seat objects,
 * availability is automatically separate for each show.
 */
public class Seat {

    private final String seatNumber;   // e.g. "A1"
    private boolean booked;

    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
        this.booked = false;
    }

    public String getSeatNumber() { return seatNumber; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    @Override
    public String toString() {
        return seatNumber + (booked ? "(booked)" : "");
    }
}
