package funemployed.game;

import funemployed.game.providers.DeckFromFile;
import funemployed.game.providers.DeckProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Service
public class GameInstanceFactory {
    private static int GAME_ID_LENGTH = 5;
    private static int MAX_NEWGAME_ATTEMPTS = 3;

    @Value("${packs.location}")
    private String cardPacksPath;

    private HashMap<String, List<DeckProvider>> deckProviderRegistry = new HashMap<>();
    private HashMap<String, GameInstance> gameRegistry = new HashMap<String, GameInstance>();

    public void setCardPacksPath(String path){
        cardPacksPath = path;
    }

    public static String generateGameId(int idLength){
        char[] gid = new char[idLength];
        for(int i = 0; i < idLength; ++i){
            gid[i] = (char) ('A' + (Math.random()*100)%26);
        }

        return new String(gid);
    }

    public static String cleanLanguageParameter(String language){
        return language.replaceAll("\\.*/*\\*","");
    }

    public List<DeckProvider> resolveOrCreateDeckProviders(String language){
        language = cleanLanguageParameter(language);
        synchronized (deckProviderRegistry){
            if(deckProviderRegistry.containsKey(language)) {
                return deckProviderRegistry.get(language);
            } else {
                List<DeckProvider> deckProviders = new LinkedList<>();
                deckProviders.add(new DeckFromFile(cardPacksPath + "/" + language + "/jobs"));
                deckProviders.add(new DeckFromFile(cardPacksPath + "/" + language + "/traits"));

                deckProviderRegistry.put(language, deckProviders);
                return deckProviders;
            }
        }
    }

    public GameInstance newGame(String language){
        String gid = null;
        for(int i = 0; i < MAX_NEWGAME_ATTEMPTS; ++i){
            gid = generateGameId(GAME_ID_LENGTH);
            synchronized (gameRegistry){
                if(!gameRegistry.containsKey(gid)){
                    break;
                }
            }
        }

        if(gid == null){
            return null;
        }

        List<DeckProvider> deckProviders = resolveOrCreateDeckProviders(language);
        if(deckProviders == null || deckProviders.size() != 2){
            return null;
        }

        GameInstance gameInstance = new GameInstance(gid, deckProviders.get(0).newDeck(), deckProviders.get(1).newDeck());
        synchronized (gameRegistry) {
            gameRegistry.put(gameInstance.getId(), gameInstance);
        }
        return gameInstance;
    }

    public GameInstance findGame(String gameId){
        synchronized(gameRegistry) {
            return gameRegistry.get(gameId);
        }
    }

    public GameInstance endGame(String gameId){
        synchronized (gameRegistry){
            return gameRegistry.remove(gameId);
        }
    }
}
