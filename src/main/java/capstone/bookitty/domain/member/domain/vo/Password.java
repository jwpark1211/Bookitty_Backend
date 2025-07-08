package capstone.bookitty.domain.member.domain.vo;

public class Password {
    private final String rawPassword;

    public Password(String rawPassword) {
        validate(rawPassword);
        this.rawPassword = rawPassword;
    }

    private void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        // 영문 대소문자 + 숫자 + 특수문자 1개 이상씩 포함, 길이 8~20자
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            throw new IllegalArgumentException("Passwords must contain at least one English case, number, and " +
                    "special characters, and must be 8 or more and 20 or less in length");
        }
    }

    public String getRaw() {
        return rawPassword;
    }
}
