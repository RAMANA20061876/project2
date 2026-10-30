package com.services;

import com.models.Booking;
import com.repository.BookingHistoryRepository;

import java.util.List;

public class BookingHistoryService {

    private final BookingHistoryRepository bookingHistoryRepository;

    public BookingHistoryService(
            BookingHistoryRepository bookingHistoryRepository) {

        this.bookingHistoryRepository = bookingHistoryRepository;
    }

    public List<Booking> getBookingHistory(int userId) {

        return bookingHistoryRepository.findByUserId(userId);
    }
}
