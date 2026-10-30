package com.models;

import java.time.LocalDateTime;

public class Review {

    private final int reviewId;
    private final User user;
    private final Movie movie;
    private final int rating;
    private final String comment;
    private final LocalDateTime reviewDate;

    public Review(int reviewId, User user, Movie movie,
                  int rating, String comment) {

        this.reviewId = reviewId;
        this.user = user;
        this.movie = movie;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = LocalDateTime.now();
    }

    public int getReviewId() {
        return reviewId;
    }

    public User getUser() {
        return user;
    }

    public Movie getMovie() {
        return movie;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }
}
