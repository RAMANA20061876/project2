import cli.PaymentCLI;
import com.movieticket.cli.SeatSelectionCLI;
import model.Certificate;
import model.Genre;
import model.Movie;
import model.Screen;
import model.Theatre;
import model.UserRole;
import repository.OfferRepository;
import repository.PaymentRepository;
import repository.RefundRepository;
import service.Module1AdminService;
import services.OfferService;
import services.PaymentService;
import services.PricingService;
import services.RefundService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static {
        final InputStream old = System.in;
        System.setIn(new InputStream() {
            public int read() throws IOException {
                return old.read();
            }

            public int read(byte[] b, int off, int len) throws IOException {
                if (len == 0) {
                    return 0;
                }
                int x = old.read();
                if (x == -1) {
                    return -1;
                }
                b[off] = (byte) x;
                return 1;
            }
        });
    }

    static Scanner sc = new Scanner(System.in);

    static OfferService offerService = new OfferService(new OfferRepository());
    static PaymentRepository paymentRepo = new PaymentRepository();
    static RefundRepository refundRepo = new RefundRepository();
    static PaymentService paymentService = new PaymentService(paymentRepo, new PricingService());
    static RefundService refundService = new RefundService(paymentRepo, refundRepo);

    static Module1AdminService admin = new Module1AdminService();
    static int screenId = 1;

    public static void main(String[] args) {

        // some offers so the payment menu has data
        offerService.createOffer("WELCOME10", "Welcome Offer", 10, 100, LocalDate.now().plusYears(1));
        offerService.createOffer("FEST20", "Festival Offer", 20, 500, LocalDate.now().plusMonths(6));

        int choice = -1;
        while (choice != 0) {
            System.out.println();
            System.out.println("===== MOVIE TICKET BOOKING SYSTEM =====");
            System.out.println("1. Seat selection and booking");
            System.out.println("2. Payments, offers and refunds");
            System.out.println("3. Admin (movies and theatres)");
            System.out.println("0. Exit");
            choice = readInt("Enter choice: ");

            if (choice == 1) {
                SeatSelectionCLI.main(new String[0]);
            } else if (choice == 2) {
                new PaymentCLI(paymentService, offerService, refundService).start();
            } else if (choice == 3) {
                adminMenu();
            } else if (choice == 0) {
                System.out.println("Bye!");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void adminMenu() {
        UserRole role = UserRole.ADMIN;
        int choice = -1;
        while (choice != 0) {
            System.out.println();
            System.out.println("----- ADMIN -----");
            System.out.println("1. Add movie");
            System.out.println("2. View movies");
            System.out.println("3. Add theatre");
            System.out.println("4. View theatres");
            System.out.println("5. Delete theatre");
            System.out.println("0. Back");
            choice = readInt("Enter choice: ");

            try {
                if (choice == 1) {
                    addMovie(role);
                } else if (choice == 2) {
                    viewMovies();
                } else if (choice == 3) {
                    addTheatre(role);
                } else if (choice == 4) {
                    viewTheatres();
                } else if (choice == 5) {
                    admin.deleteTheatre(role, readInt("Theatre ID: "));
                } else if (choice != 0) {
                    System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static void addMovie(UserRole role) {
        String title = readText("Title: ");
        String language = readText("Language: ");
        Genre genre = Genre.valueOf(readText("Genre (ACTION, COMEDY, DRAMA, THRILLER, ANIMATION): ").toUpperCase());
        int duration = readInt("Duration in minutes: ");
        LocalDate release = LocalDate.parse(readText("Release date (yyyy-MM-dd): "));
        Certificate cert = Certificate.valueOf(readText("Certificate (U, UA, A): ").toUpperCase());

        admin.addMovie(role, title, language, genre, duration, release, cert);
    }

    static void viewMovies() {
        if (admin.getMovieRepository().findAll().isEmpty()) {
            System.out.println("No movies found.");
            return;
        }
        for (Movie m : admin.getMovieRepository().findAll().values()) {
            System.out.println(m.getId() + " | " + m.getTitle() + " | " + m.getLanguage() + " | "
                    + m.getGenre() + " | " + m.getDurationMinutes() + " min | "
                    + m.getReleaseDate() + " | " + m.getCertificate());
        }
    }

    static void addTheatre(UserRole role) {
        String name = readText("Theatre name: ");
        String city = readText("City: ");
        int count = readInt("Number of screens: ");

        List<Screen> screens = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            System.out.println("Screen " + i);
            String screenName = readText("  Name: ");
            int rows = readInt("  Rows: ");
            int seats = readInt("  Seats per row: ");
            screens.add(new Screen(screenId++, screenName, rows, seats));
        }
        admin.addTheatre(role, name, city, screens);
    }

    static void viewTheatres() {
        if (admin.getTheatreRepository().findAll().isEmpty()) {
            System.out.println("No theatres found.");
            return;
        }
        for (Theatre t : admin.getTheatreRepository().findAll().values()) {
            System.out.println(t.getId() + " | " + t.getName() + " | " + t.getCity());
            for (Screen s : t.getScreens()) {
                System.out.println("    Screen " + s.getId() + " - " + s.getName()
                        + " (" + s.getSeats().size() + " seats)");
            }
        }
    }

    static String readText(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    static int readInt(String message) {
        while (true) {
            try {
                return Integer.parseInt(readText(message));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}
