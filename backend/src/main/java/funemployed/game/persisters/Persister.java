package funemployed.game.persisters;

import funemployed.game.GameInstance;

import java.util.List;

public interface Persister {
    void save(GameInstance gi);
    GameInstance load(String gameId);
    List<GameInstance> loadAll();
}
