package funemployed.game;

import funemployed.game.errors.GameException;
import funemployed.game.errors.PlayerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GameInstanceTest {

    private static final String GAME_ID = "test";
    private GameInstance bareGameInstance;
    private GameInstance gameWithMinimumPlayersAndInfiniteCards;
    private GameInstance gameWithAllPlayersReady;
    private GameInstance gameWithFirstPlayerInterviewing;
    private GameInstance gameWithAllPlayersInterviewed;
    private GameInstance gameWithOneCompletedTurn;
    private List<Player> testers;

    @Mock
    private Deck jobs;

    @Mock
    private Deck traits;

    private List<Player> generatePlayers(int count){
        List<Player> players = new LinkedList<>();
        for(int i = 0 ; i < count; ++i){
            players.add(new Player("tester_"+i));
        }

        return players;
    }

    @Before
    public void setupGameInstance() throws Exception {
        bareGameInstance = new GameInstance(GAME_ID, jobs, traits);
        gameWithMinimumPlayersAndInfiniteCards = new GameInstance(GAME_ID, jobs, traits);
        gameWithAllPlayersReady = new GameInstance(GAME_ID, jobs, traits);
        gameWithFirstPlayerInterviewing = new GameInstance(GAME_ID, jobs, traits);
        gameWithAllPlayersInterviewed = new GameInstance(GAME_ID, jobs, traits);
        gameWithOneCompletedTurn = new GameInstance(GAME_ID, jobs, traits);

        testers = new LinkedList<>();
        testers.add(new Player("tester_1"));
        testers.add(new Player("tester_2"));
        testers.add(new Player("tester_3"));

        when(traits.numberOfRemainingCards()).thenReturn(9999);
        when(traits.draw(anyInt())).thenAnswer(c -> fakeDraw((int) c.getArguments()[0]));

        when(jobs.numberOfRemainingCards()).thenReturn(9999);
        when(jobs.draw(anyInt())).thenAnswer(c -> fakeDraw((int) c.getArguments()[0]));

        addPlayersToGame(gameWithMinimumPlayersAndInfiniteCards, testers);
        addPlayersToGame(gameWithAllPlayersReady, generatePlayers(3));
        addPlayersToGame(gameWithFirstPlayerInterviewing, generatePlayers(3));
        addPlayersToGame(gameWithAllPlayersInterviewed, generatePlayers(3));
        addPlayersToGame(gameWithOneCompletedTurn, generatePlayers(3));

        gameWithAllPlayersReady.startTurn();
        gameWithFirstPlayerInterviewing.startTurn();
        gameWithAllPlayersInterviewed.startTurn();

        readyUpPlayers(gameWithAllPlayersReady);
        readyUpPlayers(gameWithFirstPlayerInterviewing);
        readyUpPlayers(gameWithAllPlayersInterviewed);

        Player candidate = gameWithFirstPlayerInterviewing.getPlayers().get(1);
        gameWithFirstPlayerInterviewing.startInterview(candidate.getId());

        interviewAllPlayersInRound(gameWithAllPlayersInterviewed);


        runSuccessfulTurn(gameWithOneCompletedTurn);
    }

    private static void addPlayersToGame(GameInstance gameInstance, List<Player> players) {
        for(Player player: players){
            gameInstance.addPlayer(player);
        }
    }

    private static Integer GLOBAL_CARD_ID = 0;
    private static Card generateRandomCard(){
        return new Card(GLOBAL_CARD_ID++, String.valueOf(Math.random()));
    }

    private static List<Card> fakeDraw(int count){
        List<Card> cards = new LinkedList<>();
        while(count > 0){
            count--;
            cards.add(generateRandomCard());
        }

        return cards;
    }

    public static Integer[] selectCardsFromHand(Player player, int offsetStart){
        //pick cards
        List<Card> hand = player.getTraits();
        Integer[] pick = new Integer[Player.REQUIRED_CANDIDATE_CARD_COUNT];
        for (int i = offsetStart; i - offsetStart < Player.REQUIRED_CANDIDATE_CARD_COUNT; ++i) {
            pick[i - offsetStart] = hand.get(i).getId();
        }

        return pick;
    }

    public boolean validateSelection(Player player, Integer[] selection){
        List<Card> cards = player.getCandidateCards();
        Map<Integer, Boolean> matches = new HashMap<>();
        if(player.getCandidateCards().size() != selection.length){ return false; }
        for(Card card: cards) {
            for(Integer i: selection){
                if(i.equals(card.getId())){
                    matches.put(card.getId(),true);
                }
            }
        }

        return matches.keySet().size() == selection.length;
    }

    @Test(expected = GameException.class)
    public void testTurnDoestStartWithoutEnoughPlayers0() throws GameException {
        bareGameInstance.startTurn();
    }

    @Test(expected = GameException.class)
    public void testTurnDoestStartWithoutEnoughPlayers2() throws GameException {
        bareGameInstance.addPlayer(new Player("tester_1"));
        bareGameInstance.addPlayer(new Player("tester_2"));
        bareGameInstance.startTurn();
    }

    @Test(expected = GameException.class)
    public void testTurnEndFailsIfNoTurnInProgressBeforeCheckingPlayerId() throws GameException, PlayerException {
        bareGameInstance.endTurn("tester_1");
    }

    @Test(expected = GameException.class)
    public void turnDoesNotEndWithoutInterviewing() throws GameException, PlayerException {
        try{
            gameWithMinimumPlayersAndInfiniteCards.startTurn();
        } catch(Exception e){
            fail();
        }
        gameWithMinimumPlayersAndInfiniteCards.endTurn(testers.get(1).getId());
    }

    @Test
    public void addPlayerWorks() throws Exception{
        Player player = new Player("black_sheep");
        int beforeCount = gameWithMinimumPlayersAndInfiniteCards.getPlayers().size();
        gameWithMinimumPlayersAndInfiniteCards.addPlayer(player);
        int afterCount = gameWithMinimumPlayersAndInfiniteCards.getPlayers().size();

        assertEquals(player, gameWithMinimumPlayersAndInfiniteCards.getPlayer(player.getId()));
        assertEquals(beforeCount + 1, afterCount);
    }

    @Test
    public void addingDuplicatePlayerReturnsExistingPlayer(){
        Player player = new Player("black_sheep");
        gameWithMinimumPlayersAndInfiniteCards.addPlayer(player);
        Player samePlayer = new Player(player.getId());
        Player returned = gameWithMinimumPlayersAndInfiniteCards.addPlayer(samePlayer);
        assertEquals(player, samePlayer);
        assertEquals(player, returned);
        assertFalse(player == samePlayer);
        assertEquals(testers.size() + 1, gameWithMinimumPlayersAndInfiniteCards.getPlayers().size());
    }

    @Test(expected = GameException.class)
    public void removeExistingPlayerWorks() throws Exception {
        gameWithMinimumPlayersAndInfiniteCards.removePlayer("tester_1");
        assertEquals(testers.size() - 1, gameWithMinimumPlayersAndInfiniteCards.getPlayers().size());

        gameWithMinimumPlayersAndInfiniteCards.getPlayer("tester_1");
    }

    @Test(expected = GameException.class)
    public void removingPlayerTwiceFails() throws GameException {
        try {
            gameWithMinimumPlayersAndInfiniteCards.removePlayer("tester_1");
        }catch(GameException e){
            fail();
        }

        gameWithMinimumPlayersAndInfiniteCards.removePlayer("tester_1");
    }

    @Test
    public void testEmployerLeavesDuringTurn() throws Exception {
        gameWithMinimumPlayersAndInfiniteCards.startTurn();
        for(int i = 1; i < testers.size(); ++i){
            Player player =gameWithMinimumPlayersAndInfiniteCards.getPlayer(testers.get(i).getId());
            gameWithMinimumPlayersAndInfiniteCards.playerReady(player.getId(), selectCardsFromHand(player,0));
        }

        gameWithMinimumPlayersAndInfiniteCards.removePlayer("tester_1");
        assertEquals(testers.size() - 1, gameWithMinimumPlayersAndInfiniteCards.getPlayers().size());
        //ToDo
    }

    @Test
    public void testEmployerLeavesDuringInterview() throws Exception {
        gameWithMinimumPlayersAndInfiniteCards.startTurn();
        gameWithMinimumPlayersAndInfiniteCards.removePlayer("tester_1");
    }

    @Test
    public void testCandidatesArePickedInOrder(){

        for(int i = 0; i < 3; ++i) {
            for (Player player : testers) {
                gameWithMinimumPlayersAndInfiniteCards.forceNewTurn();
                assertEquals(player, gameWithMinimumPlayersAndInfiniteCards.getCurrentEmployer());
            }
        }
    }

    @Test
    public void testReadyPlayerWorks() throws Exception {
        Player p = gameWithMinimumPlayersAndInfiniteCards.getPlayers().get(1);
        gameWithMinimumPlayersAndInfiniteCards.startTurn();

        Integer[] pick = selectCardsFromHand(p, 0);
        gameWithMinimumPlayersAndInfiniteCards.playerReady(p.getId(), pick);
        gameWithMinimumPlayersAndInfiniteCards.playerReady(p.getId(), pick);

        assertEquals(pick.length, p.getCandidateCards().size());
        assertTrue(validateSelection(p, pick));

        gameWithMinimumPlayersAndInfiniteCards.playerUnready(p.getId());
        assertEquals(0, p.getCandidateCards().size());
    }

    @Test
    public void testDoubleReadyPlayerWorksWithDifferentCards() throws Exception {
        Player p = gameWithMinimumPlayersAndInfiniteCards.getPlayers().get(1);
        gameWithMinimumPlayersAndInfiniteCards.startTurn();

        Integer[] pick = selectCardsFromHand(p, 0);
        Integer[] secondPick = selectCardsFromHand(p, 3);
        Integer[] thirdPick  = selectCardsFromHand(p, 1);

        gameWithMinimumPlayersAndInfiniteCards.playerReady(p.getId(), pick);
        assertTrue(validateSelection(p, pick));

        gameWithMinimumPlayersAndInfiniteCards.playerReady(p.getId(), secondPick);
        assertTrue(validateSelection(p, secondPick));

        gameWithMinimumPlayersAndInfiniteCards.playerReady(p.getId(), thirdPick);
        assertTrue(validateSelection(p, thirdPick));

        gameWithMinimumPlayersAndInfiniteCards.playerUnready(p.getId());
        assertEquals(0, p.getCandidateCards().size());
    }

    @Test(expected = GameException.class)
    public void testUnredyDuringNoTurn() throws GameException {
        Player p = gameWithMinimumPlayersAndInfiniteCards.getPlayers().get(1);
        gameWithMinimumPlayersAndInfiniteCards.playerUnready(p.getId());
    }

    @Test
    public void testUnreadyDuringInterview() throws Exception {
        List<Player> players = gameWithFirstPlayerInterviewing.getPlayers();
        for(Player p: players){
            boolean readyBefore = p.isReady();
            int cardCountBefore = p.getCandidateCards().size();
            try {
                gameWithFirstPlayerInterviewing.playerUnready(p.getId());
                fail();
            } catch(GameException e){

            }
            assertEquals(readyBefore, p.isReady());
            assertEquals(cardCountBefore, p.getCandidateCards().size());
        }
    }

    //start interview scenarios: works, no turn, alreay in progress, player does not exist, player not ready, player is employer
    @Test
    public void testStartInterviewWorks(){

    }


    //start turn cases: works, turn in progress, enough players, enough cards
    //player ready cases: works, no turn, incorrect player(including employer), already interviewed, incorrect number of cards, invalid cards, interviewing started (cannot re-ready till next turn)
    //player unready cases: works, no turn, incorrect player, interviewing started (cannot unready till next turn)
    //start interview cases: works, no turn, interview in progress, incorrect player(including employer), player already interviewed
    //reveal card cases: works, no turn, no interview, incorrect player, incorrect card
    //end interview cases: works, no turn, no interview
    //end turn cases: wokrs, no turn in progress, interviewing is not done yet

    //add player cases: works +turn_not_started(cannot acomodate) +turn_started(cannot_acomodate)
    //remove player cases: works +turn_started +is_being_interviewed +is_interviewer
    private void readyUpPlayers(GameInstance game) throws PlayerException {
        List<Player> players = game.getPlayers();
        //players redy
        int timesFailed = 0;
        for(Player player: players) {

            try {
                game.playerReady(player.getId(), selectCardsFromHand(player, 0));
            } catch(GameException e){
                timesFailed ++;
            }
        }
        assertTrue(timesFailed == 1);
    }

    private Player interviewAllPlayersInRound(GameInstance game) throws GameException, PlayerException {
        List<Player> players = game.getPlayers();
        Player winner = null;
        int timesFailed = 0;

        for(Player player: players) {
            if(!player.equals(game.getCurrentEmployer())){
                winner = player;
            }

            try {
                game.startInterview(player.getId());
            } catch (GameException e) {
                timesFailed++;
                continue;
            }

            //reveal cards
            List<Card> selection = player.getCandidateCards();
            for(Card card: selection){
                game.revealCard(player.getId(), card.getId());
            }
        }
        assertTrue(timesFailed == 1);

        return winner;
    }

    private void runSuccessfulTurn(GameInstance game) throws Exception {
        List<Player> players = game.getPlayers();

        game.startTurn();
        readyUpPlayers(game);
        Player winner = interviewAllPlayersInRound(game);

        //winner
        try{
            game.endTurn(game.getCurrentEmployer().getId());
            fail();
        } catch(GameException e){

        }
        game.endTurn(winner.getId());
    }

    @Test(expected = GameException.class)
    public void runTillTheEndOfTheJobsDeck() throws Exception {
        Deck actualJobs = new Deck(fakeDraw(3));

        //make game
        GameInstance game = new GameInstance(GAME_ID, actualJobs, traits);
        //add players
        addPlayersToGame(game, testers);
        //run game
        Assert.assertEquals(testers.size(), game.getPlayers().size());

        for(int i =0 ; i < 3; ++i) {
            try {
                runSuccessfulTurn(game);
            } catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        //try to start another turn, should fail due to not enough job cards reamining
        game.startTurn();
    }

    @Test(expected = GameException.class)
    public void runTillTheEndOfTheTraitsDeck() throws Exception {
        Deck actualTraits = new Deck(fakeDraw(
                Player.MAX_HAND_CARD_COUNT * testers.size() //enough cards for the starting hand for all players
                + ((testers.size() - 1) * Player.REQUIRED_CANDIDATE_CARD_COUNT) * 2 //enough draw cards for 2 rounds
                + (testers.size() - 2) * Player.REQUIRED_CANDIDATE_CARD_COUNT //cards just enough for the number of candidates - 1
        ));

        //make game
        GameInstance game = new GameInstance(GAME_ID, jobs, actualTraits);
        //add players
        addPlayersToGame(game, testers);
        //run game
        Assert.assertEquals(testers.size(), game.getPlayers().size());

        for(int i =0 ; i < 3; ++i) {
            try {
                runSuccessfulTurn(game);
            } catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        //try to start another turn, should fail due to not enough trait cards remaining
        game.startTurn();
    }
}
