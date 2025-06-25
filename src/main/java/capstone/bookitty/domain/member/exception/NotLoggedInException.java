package capstone.bookitty.domain.member.exception;


public class NotLoggedInException extends RuntimeException {

    private static final String MESSAGE = "User is not logged in or refresh token is missing.";

    public NotLoggedInException() {
        super(MESSAGE);
    }

    public NotLoggedInException(Throwable cause) {
        super(MESSAGE, cause);
    }
}

