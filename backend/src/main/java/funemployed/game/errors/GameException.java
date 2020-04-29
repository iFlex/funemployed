package funemployed.game.errors;

public class GameException extends Exception {
    public String message;
    public GameException(String message){
        super(message);
        this.message = message;
    }
}
