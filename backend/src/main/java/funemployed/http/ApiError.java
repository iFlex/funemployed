package funemployed.http;

public class ApiError {
    String errorCode;
    String errorMessage;

    public ApiError(String code, String message){
        this.errorCode = code;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
