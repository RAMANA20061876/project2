package cli;

import models.Role;
import models.Show;
import services.ShowSchedulingException;
import services.ShowServices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class ShowCLI {

    private Role role;          // comes from the login module, not typed here
    private String userName;
    private ShowServices services = new ShowServices();
    private Scanner sc = new Scanner(System.in);

    public ShowCLI(Role role, String userName) {
        this.role = role;
        this.userName = userName;
    }

    public void run() {
        int choice = -1;
        while (choice != 0) {
            System.out.println();
            System.out.println("===== Show Scheduling =====");
            System.out.println("1. Create show");
            System.out.println("2. View all shows");
            System.out.println("3. View upcoming shows");
            System.out.println("4. View show by ID");
            System.out.println("5. View shows by movie");
            System.out.println("6. View shows by screen");
            System.out.println("7. View shows by date");
            System.out.println("8. Update show schedule");
            System.out.println("9. Update show prices");
            System.out.println("10. Cancel show");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
                if (choice == 1) {
                    createShow();
                } else if (choice == 2) {
                    printShows(services.getAllShows(role));
                } else if (choice == 3) {
                    printShows(services.getUpcomingShows(role));
                } else if (choice == 4) {
                    System.out.println(services.getShowById(role, readInt("Show ID: ")));
                } else if (choice == 5) {
                    printShows(services.getShowsByMovie(role, readInt("Movie ID: ")));
                } else if (choice == 6) {
                    printShows(services.getShowsByScreen(role, readInt("Screen ID: ")));
                } else if (choice == 7) {
                    printShows(services.getShowsByDate(role, readDate()));
                } else if (choice == 8) {
                    updateSchedule();
                } else if (choice == 9) {
                    updatePrices();
                } else if (choice == 10) {
                    cancelShow();
                } else if (choice != 0) {
                    System.out.println("Invalid choice.");
                }
            } catch (ShowSchedulingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    private void createShow() {
        int movieId = readInt("Movie ID: ");
        int screenId = readInt("Screen ID: ");
        LocalDateTime start = readDateTime("Start time (yyyy-MM-ddTHH:mm): ");
        BigDecimal regular = readMoney("Regular price: ");
        BigDecimal premium = readMoney("Premium price: ");
        BigDecimal recliner = readMoney("Recliner price: ");

        Show show = services.createShow(role, movieId, screenId, start, regular, premium, recliner);
        System.out.println("Show created: " + show);
    }

    private void updateSchedule() {
        int showId = readInt("Show ID: ");

        System.out.print("New movie ID (press Enter to keep): ");
        String movieText = sc.nextLine().trim();
        int movieId = movieText.isEmpty() ? 0 : Integer.parseInt(movieText);

        System.out.print("New screen ID (press Enter to keep): ");
        String screenText = sc.nextLine().trim();
        int screenId = screenText.isEmpty() ? 0 : Integer.parseInt(screenText);

        System.out.print("New start time yyyy-MM-ddTHH:mm (press Enter to keep): ");
        String startText = sc.nextLine().trim();
        LocalDateTime start = startText.isEmpty() ? null : LocalDateTime.parse(startText);

        Show show = services.updateShow(role, showId, movieId, screenId, start);
        System.out.println("Updated: " + show);
    }

    private void updatePrices() {
        int showId = readInt("Show ID: ");
        BigDecimal regular = readMoney("New regular price: ");
        BigDecimal premium = readMoney("New premium price: ");
        BigDecimal recliner = readMoney("New recliner price: ");

        Show show = services.updateShowPrices(role, showId, regular, premium, recliner);
        System.out.println("Updated: " + show);
    }

    private void cancelShow() {
        int showId = readInt("Show ID: ");
        System.out.print("Reason for cancellation: ");
        String reason = sc.nextLine();
        System.out.println(services.cancelShow(role, userName, showId, reason));
    }

    private void printShows(List<Show> shows) {
        if (shows.isEmpty()) {
            System.out.println("No shows found.");
            return;
        }
        for (Show show : shows) {
            System.out.println(show);
        }
    }

    private int readInt(String message) {
        System.out.print(message);
        return Integer.parseInt(sc.nextLine().trim());
    }

    private BigDecimal readMoney(String message) {
        System.out.print(message);
        return new BigDecimal(sc.nextLine().trim());
    }

    private LocalDate readDate() {
        System.out.print("Date (yyyy-MM-dd): ");
        return LocalDate.parse(sc.nextLine().trim());
    }

    private LocalDateTime readDateTime(String message) {
        System.out.print(message);
        return LocalDateTime.parse(sc.nextLine().trim());
    }
}