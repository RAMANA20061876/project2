package model;

import java.time.LocalDate;

public class Movie {
    private int id;
    private String title;
    private String language;
    private Genre genre;
    private int durationMinutes;
    private LocalDate releaseDate;
    private Certificate certificate;

    public Movie(int id, String title, String language, Genre genre, int durationMinutes, LocalDate releaseDate, Certificate certificate) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.certificate = certificate;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getLanguage() { return language; }
    public Genre getGenre() { return genre; }
    public int getDurationMinutes() { return durationMinutes; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public Certificate getCertificate() { return certificate; }
}