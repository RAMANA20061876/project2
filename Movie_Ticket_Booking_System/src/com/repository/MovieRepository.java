package repository;

import model.Movie;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MovieRepository {
    private final Map<Integer, Movie> movieMap = new HashMap<>();
    private int idSequence = 1;

    public Movie save(Movie movie) {
        Movie savedMovie = new Movie(
            idSequence,
            movie.getTitle(),
            movie.getLanguage(),
            movie.getGenre(),
            movie.getDurationMinutes(),
            movie.getReleaseDate(),
            movie.getCertificate()
        );
        movieMap.put(idSequence, savedMovie);
        idSequence++;
        return savedMovie;
    }

    public Optional<Movie> findById(int id) {
        return Optional.ofNullable(movieMap.get(id));
    }

    public Map<Integer, Movie> findAll() {
        return movieMap;
    }
}