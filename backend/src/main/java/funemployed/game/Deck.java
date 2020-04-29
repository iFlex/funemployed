package funemployed.game;

import funemployed.game.errors.DeckException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    //only use before drawing
    public void shuffle() {
        int rounds = 10;
        for(int i = 0 ; i < rounds; ++i){
            for(int j = drawPos; j < cards.size(); ++j){
                //pick random position to swap with
                int otherPos = ((int)(Math.random() * 100)) % (cards.size() - drawPos - 1);

                //do a swap
                Card aux = cards.get(j);
                cards.set(j, cards.get(otherPos));
                cards.set(otherPos, aux);
            }
        }
    }

    public int size(){
        return cards.size();
    }

    public int numberOfRemainingCards(){
        return (cards.size() - drawPos);
    }

    public List<Card> draw(int count) throws DeckException {
        if ( numberOfRemainingCards() < count ){
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
