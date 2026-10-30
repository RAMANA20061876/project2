package service;

import model.Certificate;
import model.Genre;
import model.Movie;
import model.Screen;
import model.Seat;
import model.SeatType;
import model.Theatre;
import model.UserRole;
import repository.MovieRepository;
import repository.TheatreRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module1AdminService {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    
    private final Map<Integer, Boolean> screenHasShowsMap = new HashMap<>();
    private final Map<Integer, Integer> theatreActiveBookingsCountMap = new HashMap<>();

    public Module1AdminService() {
        this.movieRepository = new MovieRepository();
        this.theatreRepository = new TheatreRepository();
    }

    // RULE 1: Only Admin can add Movies
    public Movie addMovie(UserRole currentUserRole, String title, String language, Genre genre, int durationMinutes, LocalDate releaseDate, Certificate certificate) {
        if (currentUserRole != UserRole.ADMIN) {
            throw new SecurityException("Permission Denied: Only an Admin can add movies!");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Movie title cannot be empty!");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero minutes!");
        }

        Movie tempMovie = new Movie(0, title, language, genre, durationMinutes, releaseDate, certificate);
        Movie savedMovie = movieRepository.save(tempMovie);
        
        System.out.println("SUCCESS: Movie '" + savedMovie.getTitle() + "' (ID: " + savedMovie.getId() + ") saved in MovieRepository.");
        return savedMovie;
    }

    // RULE 2: Only Admin can add Theatre & Screen
    public Theatre addTheatre(UserRole currentUserRole, String name, String city, List<Screen> screens) {
        if (currentUserRole != UserRole.ADMIN) {
            throw new SecurityException("Permission Denied: Only an Admin can add theatres!");
        }
        if (screens == null || screens.isEmpty()) {
            throw new IllegalArgumentException("Validation Error: A theatre must have at least one screen!");
        }

        Theatre tempTheatre = new Theatre(0, name, city, screens);
        Theatre savedTheatre = theatreRepository.save(tempTheatre);
        
        for (Screen screen : screens) {
            screenHasShowsMap.put(screen.getId(), false);
        }
        theatreActiveBookingsCountMap.put(savedTheatre.getId(), 0);

        System.out.println("SUCCESS: Theatre '" + savedTheatre.getName() + "' (ID: " + savedTheatre.getId() + ") saved in TheatreRepository with " + screens.size() + " screen(s).");
        return savedTheatre;
    }

    // RULE 3: Seat type cannot be changed once any show has been created
    public void updateSeatType(UserRole currentUserRole, Screen screen, int seatId, SeatType newSeatType) {
        if (currentUserRole != UserRole.ADMIN) {
            throw new SecurityException("Permission Denied: Only an Admin can update seat types!");
        }
        
        Boolean showsExist = screenHasShowsMap.getOrDefault(screen.getId(), false);
        if (showsExist) {
            throw new IllegalStateException("Business Rule Violation: Seat type cannot be changed once any show has been created on this screen!");
        }

        for (Seat seat : screen.getSeats()) {
            if (seat.getId() == seatId) {
                seat.setSeatType(newSeatType);
                System.out.println("SUCCESS: Seat ID " + seatId + " updated to " + newSeatType + ".");
                return;
            }
        }
    }

    // RULE 4: Cannot delete theatre if active bookings exist
    public void deleteTheatre(UserRole currentUserRole, int theatreId) {
        if (currentUserRole != UserRole.ADMIN) {
            throw new SecurityException("Permission Denied: Only an Admin can delete a theatre!");
        }

        int activeBookings = theatreActiveBookingsCountMap.getOrDefault(theatreId, 0);
        if (activeBookings > 0) {
            throw new IllegalStateException("Business Rule Violation: Theatre cannot be deleted because it has upcoming shows with active bookings!");
        }

        boolean deleted = theatreRepository.deleteById(theatreId);
        if (deleted) {
            System.out.println("SUCCESS: Theatre ID " + theatreId + " removed from TheatreRepository.");
        }
    }

    public void markShowCreatedForScreen(int screenId) {
        screenHasShowsMap.put(screenId, true);
    }

    public void updateActiveBookingsCount(int theatreId, int count) {
        theatreActiveBookingsCountMap.put(theatreId, count);
    }

    public MovieRepository getMovieRepository() { return movieRepository; }
    public TheatreRepository getTheatreRepository() { return theatreRepository; }
}