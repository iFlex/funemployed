package funemployed.game;

import funemployed.game.errors.GameException;
import funemployed.game.errors.PlayerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GameInstanceTest {

    private static final String GAME_ID = "test";
    private GameInstance bareGameInstance;
    private GameInstance gameWithMinimumPlayersAndInfiniteCards;
    private List<Player> testers;

    @Mock
    private Deck jobs;

    @Mock
    private Deck traits;

    @Before
    public void setupGameInstance() throws Exception {
        bareGameInstance = new GameInstance(GAME_ID, jobs, traits);
        gameWithMinimumPlayersAndInfiniteCards = new GameInstance(GAME_ID, jobs, traits);

        testers = new LinkedList<>();
        testers.add(new Player("tester_1"));
        testers.add(new Player("tester_2"));
        testers.add(new Player("tester_3"));

        when(traits.numberOfRemainingCards()).thenReturn(9999);
        when(traits.draw(6)).thenReturn(fakeDraw(6));
        when(traits.draw(3)).thenReturn(fakeDraw(3));
        when(traits.draw(1)).thenReturn(fakeDraw(1));

        when(jobs.numberOfRemainingCards()).thenReturn(9999);
        when(jobs.draw(1)).thenReturn(fakeDraw(1));

        addPlayersToGame(gameWithMinimumPlayersAndInfiniteCards, testers);
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

    public static Integer[] selectFirstThreeCards(Player player){
        //pick cards
        List<Card> hand = player.getTraits();
        Integer[] pick = new Integer[Player.REQUIRED_CANDIDATE_CARD_COUNT];
        for (int i = 0; i < Player.REQUIRED_CANDIDATE_CARD_COUNT; ++i) {
            pick[i] = hand.get(i).getId();
        }
        return pick;
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
            gameWithMinimumPlayersAndInfiniteCards.playerReady(player.getId(), selectFirstThreeCards(player));
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

    private void runSuccessfulTurn(GameInstance game) throws Exception {
        List<Player> players = game.getPlayers();
        Player winner = null;

        game.startTurn();

        //players redy
        int timesFailed = 0;
        for(Player player: players) {
            if(!player.equals(game.getCurrentEmployer())){
                winner = player;
            }

            try {
                game.playerReady(player.getId(), selectFirstThreeCards(player));
            } catch(GameException e){
                timesFailed ++;
            }
        }
        assertTrue(timesFailed == 1);

        //interviews
        timesFailed = 0;
        for(Player player: players) {
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
        Deck actualTraits = new Deck(fakeDraw(9999));
        //make game
        GameInstance game = new GameInstance(GAME_ID, actualJobs, actualTraits);
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
        Deck actualJobs = new Deck(fakeDraw(9999));

        when(jobs.numberOfRemainingCards()).thenReturn(9999);
        when(jobs.draw(1)).thenReturn(fakeDraw(1));
        //make game
        GameInstance game = new GameInstance(GAME_ID, actualJobs, actualTraits);
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
