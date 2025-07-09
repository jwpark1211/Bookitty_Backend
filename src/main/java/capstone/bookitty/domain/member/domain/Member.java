package capstone.bookitty.domain.member.domain;

import capstone.bookitty.global.converter.Converters;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = {"email"})
})
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String profileImg;
    private String email;

    @Column(name = "password")
    private String encodedPassword;
    private LocalDate birthDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @Convert(converter = Converters.GenderConverter.class)
    private Gender gender;

    @Convert(converter = Converters.AuthorityConverter.class)
    private Authority authority;

    private static final String DEFAULT_PROFILE_IMG = "https://bookitty-bucket.s3.ap-northeast-2.amazonaws.com/Jiji.jpeg";

    public static Member createUser(String name, String email, String encodedPassword,
                                    Gender gender, LocalDate birthDate) {
        return Member.builder()
                .name(name)
                .email(email)
                .encodedPassword(encodedPassword)
                .gender(gender)
                .birthDate(birthDate)
                .authority(Authority.ROLE_USER)
                .build();
    }

    @Builder
    private Member(String name, String email, String encodedPassword, String profileImg,
                   Gender gender, LocalDate birthDate, Authority authority) {
        this.name = name;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.profileImg = StringUtils.hasText(profileImg) ? profileImg : DEFAULT_PROFILE_IMG;
        this.gender = gender;
        this.birthDate = birthDate;
        this.createdAt = LocalDateTime.now();
        this.authority = authority;
    }

    public void validatePermissionTo(Member target) {
        if (!(this.authority == Authority.ROLE_ADMIN || this.id.equals(target.id)))
            throw new IllegalArgumentException("Access denied: You do not have permission to perform this action.");
    }
}
