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
    private List<Card> candidateCards = new ArrayList<Card>(REQUIRED_CANDIDATE_CARD_COUNT);
    private List<Card> wonCards = new LinkedList<Card>();

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

    @Override
    public boolean equals(Object other){
        if (other instanceof Player){
            return this.id.equals(((Player)other).id);
        }
        return false;
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
                if(card.getId().equals(id)){
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
        for(Card card: candidateCards){
            revealed = revealed && card.isRevealed();
        }

        return revealed;
    }

    public void revealCard(Integer cardId) throws PlayerException {
        boolean found = false;
        for(Card card: candidateCards){
            if(card.getId().equals(cardId)){
                card.setRevealed(true);
                found = true;
                break;
            }
        }

        if(!found){
            throw new PlayerException("Invalid cardId(" + cardId + ") when trying to reveal it");
        }
    }

    public void moveHandToCandidateCard(Card card){
        traits.remove(card);
        candidateCards.add(card);
        card.setRevealed(false);
    }

    public void     moveCandidateCardsToHand(){
        Iterator<Card> iterator = candidateCards.iterator();
        while(iterator.hasNext()){
            Card next = iterator.next();
            traits.add(next);
            next.setRevealed(false);
            iterator.remove();
        }
    }

    public void dropCandidateCards(){
        candidateCards.clear();
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
        if(wonCards.contains(winner)){
            throw new PlayerException("Attempted to add duplicate won card to player");
        }

        wonCards.add(winner);
    }

    public int getRefillCardCount(){
        return MAX_HAND_CARD_COUNT - traits.size();
    }

    public List<Card> getTraits() {
        return traits;
    }

    public List<Card> getCandidateCards() {
        return candidateCards;
    }

    public List<Card> getWonCards() {
        return wonCards;
    }

    public String toString(){
        return this.getClass().getSimpleName()+"::"+id;
    }
}
