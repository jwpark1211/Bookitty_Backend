package capstone.bookitty.domain.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = {"email"})
})
public class Member {

    @Id @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String profileImg;
    private String email;
    private String password;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Authority authority;

    private static final String DEFAULT_PROFILE_IMG = "https://bookitty-bucket.s3.ap-northeast-2.amazonaws.com/Jiji.jpeg";

    public static Member create(String name, String email, String password,
                                String profileImg, Gender gender, LocalDate birthDate, PasswordEncoder passwordEncoder) {
        validatePassword(password);
        return new Member(name, email, passwordEncoder.encode(password), profileImg, gender, birthDate);
    }

    @Builder
    private Member(String name, String email, String encodedPassword, String profileImg,
                  Gender gender, LocalDate birthDate){
        this.name =  name;
        this.email = email;
        this.gender = gender;
        this.password = encodedPassword;
        this.birthDate = birthDate;
        this.createdAt = LocalDateTime.now();
        this.profileImg = profileImg != null ? profileImg : DEFAULT_PROFILE_IMG;
        this.authority = Authority.ROLE_USER;
    }

    private static void validatePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank.");
        }

        //비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.
        if (!encodedPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character, and be between 8 and 20 characters long.");
        }
    }
}
