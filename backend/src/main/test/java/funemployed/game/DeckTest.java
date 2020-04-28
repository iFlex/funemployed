package funemployed.game;

import funemployed.game.errors.DeckException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(BlockJUnit4ClassRunner.class)
public class DeckTest {

    private List<Card> cards;
    private Deck deckWithCards;

    @Before
    public void initCards(){
        cards = new LinkedList<>();
        cards.add(new Card(1, "first"));
        cards.add(new Card(2, "second"));
        cards.add(new Card(3, "third"));
        cards.add(new Card(4, "fourth"));

        deckWithCards = new Deck(cards);
    }

    @Test
    public void testDrawWorks() throws Exception {
        List<Card> result = deckWithCards.draw(1);
        assertEquals(1, result.size());
        assertEquals(cards.get(0),result.get(0));
        assertEquals(cards.size() - 1, deckWithCards.numberOfRemainingCards());
    }

    @Test
    public void testDrawWorks_All() throws Exception {
        List<Card> result = deckWithCards.draw(cards.size());
        assertEquals(cards.size(), result.size());

        assertEquals(cards, result);
        assertEquals(0, deckWithCards.numberOfRemainingCards());
    }

    @Test
    public void testDrawWorks_allOneByOne() throws Exception {
        for(int i = 0 ; i < cards.size(); ++i ){
            Card result = deckWithCards.draw(1).get(0);
            assertEquals(result, cards.get(i));
            assertEquals(cards.size() - i - 1, deckWithCards.numberOfRemainingCards());
        }
    }

    @Test
    public void testDrawWorks_nonUniformGroups() throws Exception {
        List<Card> first = deckWithCards.draw(1);
        assertEquals(cards.size() - 1, deckWithCards.numberOfRemainingCards());
        List<Card> mid = deckWithCards.draw(2);
        assertEquals(1, deckWithCards.numberOfRemainingCards());
        List<Card> last = deckWithCards.draw(1);
        assertEquals(0, deckWithCards.numberOfRemainingCards());

        assertEquals(first.get(0), cards.get(0));
        assertEquals(mid.get(0), cards.get(1));
        assertEquals(mid.get(1), cards.get(2));
        assertEquals(last.get(0), cards.get(3));
    }

    @Test(expected = DeckException.class)
    public void testDrawFails() throws Exception {
        deckWithCards.draw(5);
    }
}
