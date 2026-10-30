package model;

import java.util.ArrayList;
import java.util.List;

public class Screen {
    private int id;
    private String name;
    private int totalRows;
    private int seatsPerRow;
    private List<Seat> seats;

    public Screen(int id, String name, int totalRows, int seatsPerRow) {
        this.id = id;
        this.name = name;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.seats = new ArrayList<>();
        generateDefaultSeats();
    }

    private void generateDefaultSeats() {
        int seatIdCounter = 1;
        for (int r = 1; r <= totalRows; r++) {
            for (int s = 1; s <= seatsPerRow; s++) {
                SeatType type = (r == totalRows) ? SeatType.RECLINER :
                                (r > totalRows / 2) ? SeatType.PREMIUM : SeatType.REGULAR;
                seats.add(new Seat(seatIdCounter++, r, s, type));
            }
        }
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getTotalRows() { return totalRows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public List<Seat> getSeats() { return seats; }
}