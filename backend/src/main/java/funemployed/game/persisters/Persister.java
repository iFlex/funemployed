package funemployed.game.persisters;

import funemployed.game.GameInstance;

import java.util.List;

public interface Persister {
    void delete(String gameId);
    void save(GameInstance gi);
    GameInstance load(String gameId);
    List<GameInstance> loadAll();
}
