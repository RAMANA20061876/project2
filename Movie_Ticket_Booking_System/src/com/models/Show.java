// models/Show.java
package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Show {
    private int showId;
    private int movieId;
    private int screenId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal regularPrice;
    private BigDecimal premiumPrice;
    private BigDecimal reclinerPrice;
    private ShowStatus status;

    public Show() {
        this.status = ShowStatus.SCHEDULED;
    }

    public Show(int movieId, int screenId, LocalDateTime startTime, LocalDateTime endTime,
                BigDecimal regularPrice, BigDecimal premiumPrice, BigDecimal reclinerPrice) {
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.regularPrice = regularPrice;
        this.premiumPrice = premiumPrice;
        this.reclinerPrice = reclinerPrice;
        this.status = ShowStatus.SCHEDULED;
    }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getRegularPrice() { return regularPrice; }
    public void setRegularPrice(BigDecimal regularPrice) { this.regularPrice = regularPrice; }

    public BigDecimal getPremiumPrice() { return premiumPrice; }
    public void setPremiumPrice(BigDecimal premiumPrice) { this.premiumPrice = premiumPrice; }

    public BigDecimal getReclinerPrice() { return reclinerPrice; }
    public void setReclinerPrice(BigDecimal reclinerPrice) { this.reclinerPrice = reclinerPrice; }

    public ShowStatus getStatus() { return status; }
    public void setStatus(ShowStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Show " + showId + " | movie " + movieId + " | screen " + screenId
                + " | " + startTime + " to " + endTime
                + " | Regular " + regularPrice + ", Premium " + premiumPrice
                + ", Recliner " + reclinerPrice + " | " + status;
    }
}