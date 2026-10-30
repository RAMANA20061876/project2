package com.movieticket.model;

public class Movie {

    private final String movieId;
    private final String title;
    private final String certificate;      // "U", "UA" or "A"
    private final int durationMinutes;

    public Movie(String movieId, String title, String certificate, int durationMinutes) {
        this.movieId = movieId;
        this.title = title;
        this.certificate = certificate;
        this.durationMinutes = durationMinutes;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getCertificate() { return certificate; }
    public int getDurationMinutes() { return durationMinutes; }

    /** True for A-certified movies (only 18+ allowed). */
    public boolean isAdultOnly() {
        return "A".equalsIgnoreCase(certificate);
    }

    @Override
    public String toString() {
        return movieId + " - " + title + " [" + certificate + "] " + durationMinutes + " min";
    }
}
