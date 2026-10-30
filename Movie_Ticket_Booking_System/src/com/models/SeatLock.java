package com.movieticket.model;

import java.time.LocalDateTime;

public class SeatLock {

    private final String lockId;
    private final String showId;
    private final String seatNumber;
    private final String userId;
    private final LocalDateTime lockedAt;
    private final LocalDateTime expiresAt;

    public SeatLock(String lockId, String showId, String seatNumber, String userId,
                    LocalDateTime lockedAt, LocalDateTime expiresAt) {
        this.lockId = lockId;
        this.showId = showId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.lockedAt = lockedAt;
        this.expiresAt = expiresAt;
    }

    public String getLockId() { return lockId; }
    public String getShowId() { return showId; }
    public String getSeatNumber() { return seatNumber; }
    public String getUserId() { return userId; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public boolean isExpired(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return lockId + " - " + showId + ":" + seatNumber + " by " + userId;
    }
}
