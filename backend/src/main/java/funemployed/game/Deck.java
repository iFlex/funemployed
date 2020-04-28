package funemployed.game;

import funemployed.game.errors.DeckException;

import java.util.ArrayList;
import java.util.List;

public class Deck {

    private ArrayList<Card> cards;
    private int drawPos = 0;

    public Deck(List<Card> startCards){
        cards = new ArrayList<Card>(startCards.size());
        drawPos = 0;

        for(Card card: startCards){
            cards.add(card);
        }
    }

    public void shuffle(){

    }

    public int size(){
        return cards.size();
    }

    public int numberOfRemainingCardS(){
        return (cards.size() - drawPos);
    }

    public List<Card> draw(int count) throws DeckException {
        if ( numberOfRemainingCardS() < count ){
            throw new DeckException("Draw attempt is larger than the remaining set of cards");
        }

        List<Card> draw = new ArrayList<Card>(count);

        while(count > 0){
            count--;
            draw.add(cards.get(drawPos++));
        }

        return draw;
    }
}
