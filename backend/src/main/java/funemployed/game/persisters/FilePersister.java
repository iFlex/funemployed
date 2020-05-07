package funemployed.game.persisters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import funemployed.game.Card;
import funemployed.game.Deck;
import funemployed.game.GameInstance;
import funemployed.game.errors.DeckException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Component
public class FilePersister implements Persister {
    private static final String NEW_FILE_SUFFIX = "-tmp";
    private static final int MAX_CARD_BATCH = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePersister.class);

    private String basePath;
    private ObjectMapper om = new ObjectMapper();

    public FilePersister(@Value("${persisted_state}")String basePath){
        this.basePath =  basePath;
    }

    void saveDeck(Deck deck, String fullPath) throws IOException, DeckException {
        om.writeValue(new File(fullPath), deck);
    }

    Deck loadDeck(String fullPath) throws IOException {
        return om.readValue(new File(fullPath), Deck.class);
    }

    @Override
    public GameInstance load(String gid) {
        LOGGER.info("Loading persisted game {}", gid);

        String gameStateSavePath = createGameSaveDirectories(gid);
        File gameStoreFile = new File(gameStateSavePath+"/game.json");
        File jobDeckSavePath = new File(gameStateSavePath + "/jobs.json");
        File traitsDeckSavePath = new File(gameStateSavePath + "/traits.json");

        Deck jobs = null;
        Deck traits = null;
        GameInstance game = null;
        try{
            LOGGER.info("Loading jobs deck");
            jobs = om.readValue(jobDeckSavePath, Deck.class);
            LOGGER.info("Loading traits deck");
            traits = om.readValue(traitsDeckSavePath, Deck.class);
            LOGGER.info("Loading game state");
            game = om.readValue(gameStoreFile, GameInstance.class);
            game.setJobs(jobs);
            game.setTraits(traits);
        } catch (IOException e) {
            LOGGER.error("Error encountered while loading state", e);
            return null;
        }

        return game;
    }

    @Override
    public List<GameInstance> loadAll() {
        List<GameInstance> storedGames = new LinkedList<>();
        File[] directories = new File(basePath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for(File savedGame: directories){
            GameInstance gi = load(savedGame.getName());
            if(gi != null){
                storedGames.add(gi);
            }
        }

        return storedGames;
    }

    public String createGameSaveDirectories(String gid){
        String gameStateSavePath = basePath + "/" + gid;
        File gameFile = new File(gameStateSavePath);
        gameFile.mkdirs();

        return gameStateSavePath;
    }

    private String tmpPathToOriginalPath(String path, String suffix){
        int index = path.lastIndexOf(suffix);
        if(index > -1){
            return path.substring(0, index);
        }
        return null;
    }

    private void moveTmpFilesOverOldOnes(File files[], String newFileSuffix) throws IOException {
        for(File file: files){
            File output = new File(tmpPathToOriginalPath(file.getAbsolutePath(), newFileSuffix));

            //could choose to delete old and rename new, but don't know how each system implements that so it's safer
            //to simply overwrite the old file then delete the new one
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            while(reader.ready()){
                String line = reader.readLine();
                writer.write(line);
            }
            writer.close();
            reader.close();

            //delete tmp file
            file.delete();
        }
    }

    @Override
    public void save(GameInstance gameInstance) {
        String gameStateSavePath = createGameSaveDirectories(gameInstance.getId());
        File gameStoreFile = new File(gameStateSavePath + "/game.json" + NEW_FILE_SUFFIX);
        File jobDeckSavePath = new File(gameStateSavePath + "/jobs.json" + NEW_FILE_SUFFIX);
        File traitsDeckSavePath = new File(gameStateSavePath + "/traits.json" + NEW_FILE_SUFFIX);

        try {
            om.writeValue(jobDeckSavePath, gameInstance.getJobs());
            om.writeValue(traitsDeckSavePath, gameInstance.getTraits());
            om.writeValue(gameStoreFile, gameInstance);
        } catch (IOException e) {
            LOGGER.error("Failed to persist game {}", gameInstance.getId(), e);
        }

        File[] files = new File[3];
        files[0] = gameStoreFile;
        files[1] = jobDeckSavePath;
        files[2] = traitsDeckSavePath;

        //move temp saved files over original files
        try {
            moveTmpFilesOverOldOnes(files, NEW_FILE_SUFFIX);
        } catch(IOException e){
            LOGGER.error("Failed to overwirte main game persistance file with latest ones for game {}", gameInstance.getId());
        }
    }
}
