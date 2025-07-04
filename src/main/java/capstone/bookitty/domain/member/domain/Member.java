package capstone.bookitty.domain.member.domain;

import capstone.bookitty.domain.member.domain.vo.Password;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(name = "password")
    private String encodedPassword;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Authority authority;

    private static final String DEFAULT_PROFILE_IMG = "https://bookitty-bucket.s3.ap-northeast-2.amazonaws.com/Jiji.jpeg";

    public static Member create(String name, String email, Password rawPassword,
                                String profileImg, Gender gender, LocalDate birthDate, Authority authority,
                                PasswordEncoder encoder) {
        return Member.builder()
                .name(name)
                .email(email)
                .encodedPassword(rawPassword.encode(encoder))
                .profileImg(profileImg)
                .gender(gender)
                .birthDate(birthDate)
                .authority(authority)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Member(String name, String email, String encodedPassword, String profileImg,
                   Gender gender, LocalDate birthDate, Authority authority) {
        this.name = name;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.profileImg = profileImg != null ? profileImg : DEFAULT_PROFILE_IMG;
        this.gender = gender;
        this.birthDate = birthDate;
        this.createdAt = LocalDateTime.now();
        this.authority = authority;
    }

    public boolean canDelete(Member target) {
        return this.authority == Authority.ROLE_ADMIN || this.id.equals(target.id);
    }
}
