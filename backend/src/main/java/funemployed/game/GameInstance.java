package funemployed.game;
import funemployed.game.errors.DeckException;
import funemployed.game.errors.GameException;
import funemployed.game.errors.PlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GameInstance {
    private String id;
    private Deck traits;
    private Deck jobs;

    private List<Player> players = new ArrayList<>(10);
    private List<Player> historicPlayers = new LinkedList<>();
    private List<Integer> playersInterviewed = new ArrayList<>(9);

    private boolean turnInProgress = false;
    private boolean interviewInProgress = false;

    private Card currentRole;
    private Player currentEmployer;
    private Player currentCandidate;
    private int readyPlayerCount = 0;
    private int turnsPlayed = 0;
    private int turnsLeft = 0;

    public GameInstance(String id, Deck jobs, Deck traits){
        this.id = id;
        this.jobs = jobs;
        this.traits = traits;
    }

    public String getId(){
        return id;
    }

    public Object pickNext(Object current, List<?> options) throws GameException {
        if(current == null){
            return options.get(0);
        }

        if(options == null || options.size() < 2){
            throw new GameException("FATAL: List of objects is null or too short, must contain at least 2");
        }

        if(!options.contains(current)){
            throw new GameException("FATAL: Could not pick next item form list.");
        }

        return options.get((options.indexOf(current) + 1) % options.size());
    }

    public void pickNextCandidate() {
        for(Player player: players){
            if(!currentEmployer.equals(player) && !playersInterviewed.contains(player)) {
                currentCandidate = player;
                return;
            }
        }

        currentCandidate = null;
    }

    public void pickNextEmployer() {
        try {
            currentEmployer = (Player) pickNext(currentEmployer, players);
        } catch(GameException e){
            //ToDo: log fatal exception, this should not happen
            //gracefully continue by setting the current employer to the 1st player
            currentEmployer = players.get(0);
        }
    }

    public void pickNextRole() {
        try {
            currentRole = jobs.draw(1).get(0);
        } catch(DeckException de){
            System.out.println("Job Beck Draw Error! This shouldn't happen. Please fix");
            de.printStackTrace();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public synchronized Player addPlayer(Player player) {
        if(!players.contains(player)){
            if(traits.numberOfRemainingCardS() >= player.getRefillCardCount()){
                players.add(player);
                readyPlayerForNewTurn(player);
                return player;
            }

            System.out.println("Could not allow player to joing as there are not enough cards left in the traits deck");
            return null;
        }
        return player;
    }

    public Player getPlayer(String id) throws GameException {
        for(Player lookup: players){
            if(lookup.getId() == id ) {
                return lookup;
            }
        }

        throw new GameException("Invalid player id " + id);
    }

    public void computeTurnsLeft(){
        int cardCountNeed = (players.size() - 1) * 3;
        int turnsLeftByTraits = traits.numberOfRemainingCardS() / cardCountNeed;
        int turnsLeftByJobs = jobs.numberOfRemainingCardS();

        turnsLeft = Math.min(turnsLeftByTraits, turnsLeftByJobs);
    }

    public void removePlayer(String id) throws GameException {
        Player player = getPlayer(id);
        if(player != null){
            if(currentCandidate != null && player.equals(currentCandidate) && interviewInProgress){
                try{
                    endInterview();
                } catch(Exception e){
                    throw new RuntimeException(e);
                }
            }

            playersInterviewed.remove(player.getId());
            players.remove(player);
            historicPlayers.add(player);

            //ToDo: implement player returns his hand to the deck
            //ToDo: player discards his selected cards

            //Player is employer: turn needs to end immediately -> force start a new turn
            if(currentEmployer != null && player.equals(currentEmployer)){
                _startTurn();
            }
        }
    }

    public void shufflePlayerOrder() {
        Collections.shuffle(players);
    }

    public void replenishPlayerCards(Player player) {
        try {
            List<Card> refill = traits.draw(player.getRefillCardCount());
            player.addTraitCards(refill);
        } catch(DeckException | PlayerException e){
            System.out.println("Exception while trying to replenish player cards. This should not happen since the system pre checks if operations can be performed.");
            e.printStackTrace();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void readyPlayerForNewTurn(Player player){
        player.setReady(false);
        player.dropCandidateCards();
        replenishPlayerCards(player);
    }

    private boolean _startTurn() {
        computeTurnsLeft();
        if(turnsLeft > 0) {

            turnInProgress = true;
            interviewInProgress = false;
            currentCandidate = null;
            readyPlayerCount = 0;

            pickNextEmployer();
            pickNextRole();

            for (Player player : players) {
                readyPlayerForNewTurn(player);
            }
            return true;
        }
        return false;
    }

    public void startTurn() throws GameException {
        if(turnInProgress){
            throw new GameException("A turn is already in progress");
        }

        boolean success = _startTurn();
        if(success == false){
            throw new GameException("Game Over. Cards Depleted");
        }
    }

    public synchronized void playerReady(String playerId, Integer[] cards) throws GameException, PlayerException {
        Player player = getPlayer(playerId);
        if(!playersInterviewed.contains(playerId)){
            if(cards.length != Player.REQUIRED_CANDIDATE_CARD_COUNT){
                throw new GameException("Incorrect number of cards provided with player ready command");
            }
            player.setCandidateCards(cards);
            player.setReady(true);
        } else {
            throw new GameException(playerId + " has already been interviewed");
        }
    }

    public synchronized void playerUnready(String playerId) throws GameException {
        Player player = getPlayer(playerId);
        if(player.isReady() == true){
            if(playersInterviewed.size() > 0 || interviewInProgress){
                throw new GameException("Cannot allow player to unready while interviews have started");
            }

            player.setReady(false);
        }
    }

    public boolean allCandidatesReady(){
        boolean allReady = true;
        for(Player player: players){
            allReady = allReady && player.isReady();
        }

        return allReady;
    }

    public int candidatesLeftToInterview() {
        return players.size() - 1 - playersInterviewed.size();
    }

    public void startInterview(String playerId) throws GameException, PlayerException {
        if(turnInProgress) {
            Player player = getPlayer(playerId);
            if(interviewInProgress){
                throw new GameException("An interview is already in progress. Start a new one after it finishes");
            }

            if(player.equals(currentEmployer)){
                throw new GameException("Employer cannot interview themselves");
            }

            if(!player.isReady()){
                throw new GameException("Candidate not ready for interview");
            }

            interviewInProgress = true;
            currentCandidate = player;
        }
    }

    public void revealCard(String playerId, Integer cardId) throws GameException, PlayerException {
        Player player = getPlayer(playerId);
        if(currentCandidate.equals(player)){
            player.revealCard(cardId);
        } else {
            throw new GameException("Cannot reveal card if you're not interviewing");
        }
    }

    public void endInterview() throws GameException, PlayerException {
        if(interviewInProgress) {
            currentCandidate.dropCandidateCards();

            interviewInProgress = false;
            currentCandidate = null;
        } else {
            throw new GameException("No intervie win progress");
        }
    }

    public void cancelTurn() {
        turnInProgress = false;
    }

    public void endTurn(String winnerId) throws GameException, PlayerException{
        if(turnInProgress) {
            Player player = getPlayer(winnerId);
            int remainingCanidates = candidatesLeftToInterview();
            if(remainingCanidates < 1){
                player.addWonCard(currentRole);
                turnInProgress = false;
                turnsPlayed += 1;
            } else {
                throw new GameException("Candidates left to interview: "+remainingCanidates);
            }
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Integer> getPlayersInterviewed() {
        return playersInterviewed;
    }

    public boolean isTurnInProgress() {
        return turnInProgress;
    }

    public boolean isInterviewInProgress() {
        return interviewInProgress;
    }

    public Card getCurrentRole() {
        return currentRole;
    }

    public Player getCurrentEmployer() {
        return currentEmployer;
    }

    public Player getCurrentCandidate() {
        return currentCandidate;
    }

    public int getReadyPlayerCount() {
        return readyPlayerCount;
    }

    public int getTurnsPlayed() {
        return turnsPlayed;
    }

    public int getTurnsLeft() {
        return turnsLeft;
    }
}
