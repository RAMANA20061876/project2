package com.repository;

import com.models.Review;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {

    private final List<Review> reviews = new ArrayList<>();

    public void addReview(Review review) {
        reviews.add(review);
    }

    public boolean existsByUserAndMovie(int userId, int movieId) {

        for (Review review : reviews) {

            if (review.getUser().getUserId() == userId &&
                review.getMovie().getMovieId() == movieId) {

                return true;
            }
        }

        return false;
    }

    public List<Review> findByMovieId(int movieId) {

        List<Review> result = new ArrayList<>();

        for (Review review : reviews) {

            if (review.getMovie().getMovieId() == movieId) {
                result.add(review);
            }
        }

        return result;
    }

    public List<Review> findByUserId(int userId) {

        List<Review> result = new ArrayList<>();

        for (Review review : reviews) {

            if (review.getUser().getUserId() == userId) {
                result.add(review);
            }
        }

        return result;
    }
}
