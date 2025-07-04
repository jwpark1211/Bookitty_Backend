package capstone.bookitty.global.error.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    //Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    ENTITY_NOT_FOUND(404, "C003", "Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server Error"),
    INVALID_TYPE_VALUE(400, "C005", "Invalid Type Value"),
    HANDLE_ACCESS_DENIED(403, "C006", "Access is Denied"),
    MULTIPART_INVALID(400, "C007","Invalid MultiPart"),

    // Member
    EMAIL_DUPLICATION(400, "M001", "Email is Duplication"),
    REFRESH_TOKEN_SAVE_INVALID(500, "M003", "Refresh Token is invalid"),
    UNAUTHENTICATED_MEMBER(401, "M004", "Unauthenticated Member"),
    INVALID_REFRESH_TOKEN(401, "M005", "Refresh token does not match."),
    NOT_LOGGED_IN(401, "M006", "User is not logged in or refresh token is missing.")
    ;

    private final String code;
    private final String message;
    private int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
