package capstone.bookitty.domain.member.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Refresh token does not match.");
    }
}
