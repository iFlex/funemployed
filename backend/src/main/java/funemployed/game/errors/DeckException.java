package funemployed.game.errors;

public class DeckException extends Exception {
    public String message;

    public DeckException(String message){
        super(message);
        this.message = message;
    }
}
