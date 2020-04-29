package funemployed.game.errors;

public class PlayerException extends Exception {
    public String message;
    public PlayerException(String message){
        super(message);
        this.message = message;
    }
}
