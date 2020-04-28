package funemployed.game.providers;

import funemployed.game.Card;
import funemployed.game.Deck;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeckFromFile implements DeckProvider {
    private String path;

    public DeckFromFile(String pathToDeck){
        this.path = pathToDeck;
    }

    @Override
    public Deck newDeck() {
        BufferedReader br = null;
        try {
           br = new BufferedReader(new FileReader(new File(path)));
        } catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }

        List<String> rawCards = br.lines().collect(Collectors.toList());
        List<Card> cards = new ArrayList<>(rawCards.size());
        for(int i = 0 ; i < rawCards.size(); ++i){
            Card card = new Card(i, rawCards.get(i));
            cards.add(card);
        }

        return new Deck(cards);
    }
}
