package funemployed.game;

import funemployed.game.errors.PlayerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Player {
    public static final Integer MAX_HAND_CARD_COUNT = 6;
    public static final Integer REQUIRED_CANDIDATE_CARD_COUNT = 3;

    private String id;
    private boolean ready;
    private List<Card> traits      = new ArrayList<Card>(MAX_HAND_CARD_COUNT);
    private List<Card> candidate_cards = new ArrayList<Card>(REQUIRED_CANDIDATE_CARD_COUNT);
    private List<Card> won_cards   = new LinkedList<Card>();

    public Player(String id){
        this.id = id;
        this.ready = false;
    }

    public String getId(){
        return id;
    }

    public void setReady(boolean ready){
        this.ready = ready;
    }

    public boolean isReady(){
        return this.ready;
    }

    public boolean equals(Player other){
        return this.id == other.id;
    }

    public void setCandidateCards(Integer[] candidateIds) throws PlayerException {
        List<Card> cardsToMove = new ArrayList(REQUIRED_CANDIDATE_CARD_COUNT);
        if(candidateIds.length > REQUIRED_CANDIDATE_CARD_COUNT){
            throw new PlayerException("Invalid candidate card count. Too few or too many");
        }

        for(Integer id: candidateIds){
            //lookup card and move to candidate
            boolean found = false;
            for(Card card: traits){
                if(card.getId() == id){
                    moveHandToCandidateCard(card);
                    found = true;
                    break;
                }
            }

            if(!found){
                moveCandidateCardsToHand();
                throw new PlayerException("Invalid Card ID: " + id);
            }
        }
    }

    public boolean allCardsRevealed(){
        boolean revealed = true;
        for(Card card: candidate_cards){
            revealed = revealed && card.isRevealed();
        }

        return revealed;
    }

    public void revealCard(Integer cardId) throws PlayerException {
        boolean found = false;
        for(Card card: candidate_cards){
            if(card.getId() == cardId){
                card.setRevealed(true);
                found = true;
            }
        }

        if(!found){
            throw new PlayerException("Invalid cardId(" + cardId + ") when trying to reveal it");
        }
    }

    public void moveHandToCandidateCard(Card card){
        traits.remove(card);
        candidate_cards.add(card);
        card.setRevealed(false);
    }

    public void moveCandidateCardsToHand(){
        Iterator<Card> iterator = candidate_cards.iterator();
        while(iterator.hasNext()){
            Card next = iterator.next();
            traits.add(next);
            next.setRevealed(false);
            iterator.remove();
        }
    }

    public void dropCandidateCards(){
        candidate_cards.clear();
    }

    public void dropTraitCards(List<Card> toDrop) throws PlayerException{
        for(Card card: toDrop) {
            if( traits.contains(card) ){
                throw new PlayerException("Attempted to drop card from player hand that does not exist in player hand");
            }

            traits.remove(card);
        }
    }

    public void addTraitCards(List<Card> toAdd) throws PlayerException {
        if(toAdd.size() + traits.size() > MAX_HAND_CARD_COUNT){
            throw new PlayerException("Attempted to add more cards than allowed to player hand");
        }

        for(Card card: toAdd) {
            if( traits.contains(card) ){
                throw new PlayerException("Attempted to add duplicate trait card");
            }

            card.setRevealed(false);
            traits.add(card);
        }
    }

    public void addWonCard(Card winner) throws PlayerException {
        if(won_cards.contains(winner)){
            throw new PlayerException("Attempted to add duplicate won card to player");
        }

        won_cards.add(winner);
    }

    public int getRefillCardCount(){
        return MAX_HAND_CARD_COUNT - traits.size();
    }

    public List<Card> getTraits() {
        return traits;
    }

    public List<Card> getCandidate_cards() {
        return candidate_cards;
    }

    public List<Card> getWon_cards() {
        return won_cards;
    }
}
