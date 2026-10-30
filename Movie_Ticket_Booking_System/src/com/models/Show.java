package com.movieticket.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Show {

    private final String showId;
    private final Movie movie;
    private final String screenName;
    private final LocalDateTime startTime;
    private final double ticketPrice;
    private final Map<String, Seat> seats = new LinkedHashMap<>();

    /** Creates a show with rows x seatsPerRow seats named A1, A2 ... B1, B2 ... */
    public Show(String showId, Movie movie, String screenName, LocalDateTime startTime,
                double ticketPrice, int rows, int seatsPerRow) {
        this.showId = showId;
        this.movie = movie;
        this.screenName = screenName;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;

        for (int r = 0; r < rows; r++) {
            char rowLetter = (char) ('A' + r);
            for (int n = 1; n <= seatsPerRow; n++) {
                String seatNumber = rowLetter + "" + n;
                seats.put(seatNumber, new Seat(seatNumber));
            }
        }
    }

    public String getShowId() { return showId; }
    public Movie getMovie() { return movie; }
    public String getScreenName() { return screenName; }
    public LocalDateTime getStartTime() { return startTime; }
    public double getTicketPrice() { return ticketPrice; }

    /** Returns null if the seat does not exist in this show. */
    public Seat getSeat(String seatNumber) {
        return seats.get(seatNumber);
    }

    public List<Seat> getAllSeats() {
        return new ArrayList<>(seats.values());
    }

    @Override
    public String toString() {
        return showId + " - " + movie.getTitle() + " @ " + startTime;
    }
}
