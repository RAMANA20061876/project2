package com.movieticket.cli;

import com.movieticket.model.Booking;
import com.movieticket.model.Movie;
import com.movieticket.model.SeatLock;
import com.movieticket.model.Show;
import com.movieticket.model.User;
import com.movieticket.repository.InMemoryStorage;
import com.movieticket.service.BookingService;
import com.movieticket.service.SeatSelectionService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console application. This is the ONLY entry point (main method is here).
 */
public class SeatSelectionCLI {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final InMemoryStorage storage = new InMemoryStorage();
    private final SeatSelectionService seatSelectionService = new SeatSelectionService(storage);
    private final BookingService bookingService = new BookingService(storage, seatSelectionService);
    private final Scanner scanner = new Scanner(System.in);
    private boolean demoRan = false;

    public static void main(String[] args) {
        SeatSelectionCLI cli = new SeatSelectionCLI();
        cli.loadSampleData();
        cli.start();
    }

    // ---------------------------------------------------------------
    // Sample data (movies and shows)
    // ---------------------------------------------------------------
    private void loadSampleData() {
        Movie inception = new Movie("M1", "Inception", "UA", 148);
        Movie joker = new Movie("M2", "Joker", "A", 122);
        storage.saveMovie(inception);
        storage.saveMovie(joker);

        LocalDateTime now = storage.getCurrentTime();
        // 4 rows x 8 seats = 32 seats per show. Each show has its OWN seats.
        storage.saveShow(new Show("S1", inception, "Screen 1", now.plusHours(2), 250.0, 4, 8));
        storage.saveShow(new Show("S2", joker, "Screen 1", now.plusHours(3), 300.0, 4, 8));
        // This show starts in 10 minutes, so booking is already closed (closes 15 min before).
        storage.saveShow(new Show("S3", inception, "Screen 2", now.plusMinutes(10), 200.0, 4, 8));
    }

    // ---------------------------------------------------------------
    // Main menu loop
    // ---------------------------------------------------------------
    private void start() {
        System.out.println("=== Movie Ticket Booking - Seat Selection & Booking ===");
        boolean running = true;
        while (running && scanner.hasNextLine()) {
            printMenu();
            String choice = readLine("Enter choice: ");
            try {
                switch (choice) {
                    case "1": registerUser(); break;
                    case "2": showMoviesAndShows(); break;
                    case "3": viewSeats(); break;
                    case "4": selectSeats(); break;
                    case "5": createBooking(); break;
                    case "6": payForBooking(); break;
                    case "7": checkBookingStatus(); break;
                    case "8": advanceClock(); break;
                    case "9": runDemo(); break;
                    case "0":
                        running = false;
                        System.out.println("Bye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n-------------- MENU --------------");
        System.out.println("1. Register user");
        System.out.println("2. View movies and shows");
        System.out.println("3. View seats of a show");
        System.out.println("4. Select (lock) seats");
        System.out.println("5. Create booking from locked seats");
        System.out.println("6. Pay for booking (simulated)");
        System.out.println("7. Check booking status");
        System.out.println("8. Move clock forward (to test 10-minute lock expiry)");
        System.out.println("9. Run automatic demo");
        System.out.println("0. Exit");
    }

    // ---------------------------------------------------------------
    // Menu actions
    // ---------------------------------------------------------------
    private void registerUser() {
        String name = readLine("Name: ");
        String phone = readLine("Phone (10 digits): ");
        String email = readLine("Email: ");
        String dobText = readLine("Date of birth (yyyy-MM-dd): ");
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobText);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Date of birth must be in yyyy-MM-dd format.");
        }
        User user = seatSelectionService.registerUser(name, phone, email, dob);
        System.out.println("Registered successfully! Your User ID is: " + user.getUserId());
    }

    private void showMoviesAndShows() {
        System.out.println("\nMovies:");
        for (Movie movie : storage.getAllMovies()) {
            System.out.println("  " + movie);
        }
        System.out.println("\nShows:");
        for (Show show : storage.getAllShows()) {
            String closed = seatSelectionService.isBookingClosed(show) ? "  [BOOKING CLOSED]" : "";
            System.out.printf("  %s | %s (%s) | %s | %s | Rs.%.2f%s%n",
                    show.getShowId(), show.getMovie().getTitle(), show.getMovie().getCertificate(),
                    show.getScreenName(), show.getStartTime().format(FORMAT), show.getTicketPrice(), closed);
        }
        System.out.println("Current system time: " + storage.getCurrentTime().format(FORMAT));
    }

    private void viewSeats() {
        String showId = readLine("Show ID (e.g. S1): ").toUpperCase();
        printSeatMap(showId);
    }

    private void selectSeats() {
        String userId = readLine("User ID (e.g. U1): ").toUpperCase();
        String showId = readLine("Show ID (e.g. S1): ").toUpperCase();
        String seatText = readLine("Seats to lock, comma separated (e.g. A1,A2): ");

        List<String> seats = new ArrayList<>();
        for (String s : seatText.split(",")) {
            if (!s.trim().isEmpty()) {
                seats.add(s.trim());
            }
        }
        List<SeatLock> locks = seatSelectionService.lockSeats(userId, showId, seats);
        printLocks(locks);
    }

    private void createBooking() {
        String userId = readLine("User ID: ").toUpperCase();
        String showId = readLine("Show ID: ").toUpperCase();
        Booking booking = bookingService.createBooking(userId, showId);
        System.out.println("Booking created (waiting for payment).");
        printBooking(booking);
    }

    private void payForBooking() {
        String bookingId = readLine("Booking ID (e.g. B1): ").toUpperCase();
        String result = readLine("Simulate payment result - 1 = Success, 2 = Fail: ");
        boolean success = result.equals("1");
        Booking booking = bookingService.makePayment(bookingId, success);
        System.out.println(success ? "Payment SUCCESS." : "Payment FAILED. Seats released.");
        printBooking(booking);
    }

    private void checkBookingStatus() {
        String bookingId = readLine("Booking ID: ").toUpperCase();
        Booking booking = bookingService.getBooking(bookingId);
        printBooking(booking);
    }

    private void advanceClock() {
        String text = readLine("Move clock forward by how many minutes? ");
        int minutes;
        try {
            minutes = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative.");
        }
        storage.advanceClock(minutes);
        System.out.println("Clock moved forward. System time now: " + storage.getCurrentTime().format(FORMAT));
    }

    // ---------------------------------------------------------------
    // Automatic demo - shows every important rule
    // ---------------------------------------------------------------
    private void runDemo() {
        if (demoRan) {
            System.out.println("Demo already ran once (seats and users are already used). Restart the program to run again.");
            return;
        }
        demoRan = true;

        LocalDate today = storage.getCurrentTime().toLocalDate();
        String ashaId;
        String raviId;
        String kiranId;
        try {
            ashaId = seatSelectionService.registerUser("Asha", "9876543210", "asha@mail.com", today.minusYears(25)).getUserId();
            raviId = seatSelectionService.registerUser("Ravi", "9876500001", "ravi@mail.com", today.minusYears(15)).getUserId();
            kiranId = seatSelectionService.registerUser("Kiran", "9876500002", "kiran@mail.com", today.minusYears(30)).getUserId();
        } catch (IllegalArgumentException e) {
            System.out.println("Demo could not start: " + e.getMessage());
            demoRan = false;
            return;
        }
        System.out.println("Demo users: Asha=" + ashaId + " (25 yrs), Ravi=" + raviId + " (15 yrs), Kiran=" + kiranId + " (30 yrs)");
        runDemoSteps(ashaId, raviId, kiranId);
    }

    private void runDemoSteps(final String ashaId, final String raviId, final String kiranId) {
        try {
            expectBlocked("Step 1: Ravi (15 yrs) tries to book A-certified movie Joker (show S2)",
                    () -> seatSelectionService.lockSeats(raviId, "S2", Arrays.asList("A1")));

            System.out.println("\nStep 2: Asha locks A1 and A2 in show S1");
            printLocks(seatSelectionService.lockSeats(ashaId, "S1", Arrays.asList("A1", "A2")));

            expectBlocked("Step 3: Kiran tries to select A1 (already locked by Asha)",
                    () -> seatSelectionService.lockSeats(kiranId, "S1", Arrays.asList("A1")));

            expectBlocked("Step 4: Asha tries to select 7 more seats (limit is 6 per booking)",
                    () -> seatSelectionService.lockSeats(ashaId, "S1",
                            Arrays.asList("C1", "C2", "C3", "C4", "C5", "C6", "C7")));

            System.out.println("\nStep 5: Asha creates booking, but payment FAILS");
            Booking failedBooking = bookingService.createBooking(ashaId, "S1");
            printBooking(failedBooking);
            bookingService.makePayment(failedBooking.getBookingId(), false);
            printBooking(bookingService.getBooking(failedBooking.getBookingId()));
            System.out.println("Seat A1 is free again: " + seatSelectionService.getAvailableSeats("S1").contains("A1"));

            System.out.println("\nStep 6: Asha locks A1, A2 again, creates booking, payment SUCCEEDS");
            seatSelectionService.lockSeats(ashaId, "S1", Arrays.asList("A1", "A2"));
            Booking goodBooking = bookingService.createBooking(ashaId, "S1");
            bookingService.makePayment(goodBooking.getBookingId(), true);
            printBooking(bookingService.getBooking(goodBooking.getBookingId()));

            expectBlocked("Step 7: Kiran tries A1 (now CONFIRMED/booked for Asha)",
                    () -> seatSelectionService.lockSeats(kiranId, "S1", Arrays.asList("A1")));

            System.out.println("\nStep 8: Kiran locks B1, creates booking, but does NOT pay for 11 minutes");
            seatSelectionService.lockSeats(kiranId, "S1", Arrays.asList("B1"));
            Booking slowBooking = bookingService.createBooking(kiranId, "S1");
            storage.advanceClock(11);
            System.out.println("(clock moved forward by 11 minutes)");
            System.out.println("Kiran's booking status now: " + bookingService.getBookingStatus(slowBooking.getBookingId()));
            System.out.println("Seat B1 available again: " + seatSelectionService.getAvailableSeats("S1").contains("B1"));

            expectBlocked("Step 9: Asha tries show S3 (starts in less than 15 minutes)",
                    () -> seatSelectionService.lockSeats(ashaId, "S3", Arrays.asList("D1")));

            System.out.println("\nFinal seat map of show S1:");
            printSeatMap("S1");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Demo stopped unexpectedly: " + e.getMessage());
        }
    }

    /** Runs an action that SHOULD fail, and prints the reason. */
    private void expectBlocked(String title, Runnable action) {
        System.out.println("\n" + title);
        try {
            action.run();
            System.out.println("   -> Allowed (this was not expected!)");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("   -> Blocked: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Printing helpers
    // ---------------------------------------------------------------
    private void printSeatMap(String showId) {
        Map<String, String> seatMap = seatSelectionService.getSeatStatusMap(showId);
        System.out.println("Seats for show " + showId + "  (Free = available, Lock = locked, Sold = booked)");
        char lastRow = 0;
        for (Map.Entry<String, String> entry : seatMap.entrySet()) {
            char row = entry.getKey().charAt(0);
            if (lastRow != 0 && row != lastRow) {
                System.out.println();
            }
            lastRow = row;
            String code;
            if (entry.getValue().equals(SeatSelectionService.SEAT_AVAILABLE)) {
                code = "Free";
            } else if (entry.getValue().equals(SeatSelectionService.SEAT_LOCKED)) {
                code = "Lock";
            } else {
                code = "Sold";
            }
            System.out.printf("%-9s", entry.getKey() + ":" + code);
        }
        System.out.println();
    }

    private void printLocks(List<SeatLock> locks) {
        for (SeatLock lock : locks) {
            System.out.println("Locked seat " + lock.getSeatNumber() + " until " + lock.getExpiresAt().format(FORMAT));
        }
    }

    private void printBooking(Booking booking) {
        System.out.printf("Booking %s | User %s | Show %s | Seats %s | Amount Rs.%.2f | Status: %s%n",
                booking.getBookingId(), booking.getUserId(), booking.getShowId(),
                booking.getSeatNumbers(), booking.getTotalAmount(), booking.getStatus());
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }
}
