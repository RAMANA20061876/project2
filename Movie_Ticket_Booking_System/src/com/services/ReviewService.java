package com.services;

import com.models.*;
import com.repository.BookingHistoryRepository;
import com.repository.ReviewRepository;

import java.util.List;
import java.time.LocalDateTime;

public class ReviewService {

    private final BookingHistoryRepository bookingHistoryRepository;
    private final ReviewRepository reviewRepository;

    private int nextReviewId = 1;

    public ReviewService(
            BookingHistoryRepository bookingHistoryRepository,
            ReviewRepository reviewRepository) {

        this.bookingHistoryRepository = bookingHistoryRepository;
        this.reviewRepository = reviewRepository;
    }
    public boolean canReview(int userId, int movieId) {

        List<Booking> bookings =
                bookingHistoryRepository.findByUserId(userId);

        for (Booking booking : bookings) {

            if (booking.getShow().getMovie().getMovieId() == movieId &&
                booking.getStatus() == BookingStatus.CONFIRMED &&
                LocalDateTime.now().isAfter(booking.getShow().getEndTime())) {

                return true;
            }
        }

        return false;
    }

    // Add movie review
    public String addReview(
            User user, Movie movie, int rating, String comment) {

        if (rating < 1 || rating > 5) {
            return "Rating must be between 1 and 5.";
        }

        if (!canReview(user.getUserId(), movie.getMovieId())) {
            return "You need a confirmed booking for an ended show.";
        }

        if (reviewRepository.existsByUserAndMovie(
                user.getUserId(), movie.getMovieId())) {

            return "You have already reviewed this movie.";
        }

        Review review = new Review(
                nextReviewId++, user, movie, rating, comment);

        reviewRepository.addReview(review);

        updateAverageRating(movie);

        return "Review added successfully.";
    }
    public void updateAverageRating(Movie movie) {

        List<Review> reviews =
                reviewRepository.findByMovieId(movie.getMovieId());

        int total = 0;

        for (Review review : reviews) {
            total += review.getRating();
        }

        double average = (double) total / reviews.size();

        movie.setAverageRating(average);
    }

    public List<Review> getMovieReviews(int movieId) {

        return reviewRepository.findByMovieId(movieId);
    }
}
