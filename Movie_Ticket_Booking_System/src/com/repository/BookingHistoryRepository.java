package com.repository;

import com.models.Booking;
import java.util.List;

public class BookingHistoryRepository {

    private final BookingRepository bookingRepository;

    public BookingHistoryRepository(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> findByUserId(int userId) {
        return bookingRepository.findByUserId(userId);
    }
}
