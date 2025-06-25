package capstone.bookitty.domain.member.exception;

public class UnauthenticatedMemberException extends RuntimeException {

    public UnauthenticatedMemberException() {
        super("No logged-in member information found.");
    }

    public UnauthenticatedMemberException(String message) {
        super(message);
    }
}

