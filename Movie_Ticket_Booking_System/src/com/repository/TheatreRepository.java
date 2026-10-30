package repository;

import model.Theatre;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TheatreRepository {
    private final Map<Integer, Theatre> theatreMap = new HashMap<>();
    private int idSequence = 1;

    public Theatre save(Theatre theatre) {
        Theatre savedTheatre = new Theatre(
            idSequence,
            theatre.getName(),
            theatre.getCity(),
            theatre.getScreens()
        );
        theatreMap.put(idSequence, savedTheatre);
        idSequence++;
        return savedTheatre;
    }

    public Optional<Theatre> findById(int id) {
        return Optional.ofNullable(theatreMap.get(id));
    }

    public boolean deleteById(int id) {
        return theatreMap.remove(id) != null;
    }

    public Map<Integer, Theatre> findAll() {
        return theatreMap;
    }
}