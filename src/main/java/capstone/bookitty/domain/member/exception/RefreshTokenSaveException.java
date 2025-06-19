package capstone.bookitty.domain.member.exception;

public class RefreshTokenSaveException extends RuntimeException {
    public RefreshTokenSaveException(String message) {
        super(message);
    }
}