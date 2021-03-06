package funemployed.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {

    @JsonProperty
    private Integer id;
    @JsonProperty
    private String text;
    @JsonProperty
    private boolean revealed;

    //statistics
    private Integer timesSelected;
    private Integer timesWon;

    public Card(){
        //used by JSON deser
    }

    public Card(Integer id, String text){
        this.id = id;
        this.text = text;
        this.revealed = false;

        timesSelected = 0;
        timesWon = 0;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof Card) {
            return this.id.equals(((Card)other).id);
        }
        return false;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void markAsSelected(){
        this.timesSelected ++;
    }

    public void markAsWon(){
        this.timesWon ++;
    }

    public void setRevealed(boolean revealed){
        this.revealed = revealed;
    }

    public boolean isRevealed(){
        return revealed;
    }
}
