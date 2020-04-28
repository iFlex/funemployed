package funemployed.game;

public class Card {
    private Integer id;
    private String text;
    private boolean revealed;
    //statistics
    private Integer timesSelected;
    private Integer timesWon;

    public Card(Integer id, String text){
        this.id = id;
        this.text = text;
        this.revealed = false;

        timesSelected = 0;
        timesWon = 0;
    }

    public boolean equals(Card other){
        return this.id == other.id;
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
