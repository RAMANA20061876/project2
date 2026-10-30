package model;

public class Seat {
    private int id;
    private int rowNumber;
    private int seatNumber;
    private SeatType seatType;

    public Seat(int id, int rowNumber, int seatNumber, SeatType seatType) {
        this.id = id;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public int getId() { return id; }
    public int getRowNumber() { return rowNumber; }
    public int getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
}