package capstone.bookitty.domain.member.domain;

import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.domain.type.Gender;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.global.authentication.PasswordEncoder;
import capstone.bookitty.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

import static capstone.bookitty.global.converter.EnumConverters.AuthorityConverter;
import static capstone.bookitty.global.converter.EnumConverters.GenderConverter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = {"email"})
})
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String profileImg;
    private String email;

    @Embedded
    private Password password;
    private LocalDate birthDate;

    @Convert(converter = GenderConverter.class)
    private Gender gender;

    @Convert(converter = AuthorityConverter.class)
    private Authority authority;

    private static final String DEFAULT_PROFILE_IMG = "https://bookitty-bucket.s3.ap-northeast-2.amazonaws.com/Jiji.jpeg";

    @Builder
    private Member(String name, String email, Password password, String profileImg,
                   Gender gender, LocalDate birthDate, Authority authority) {

        validateMemberInfo(name, email, birthDate, gender);

        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.password = password;
        this.authority = authority == null ? Authority.ROLE_USER : authority;
        this.profileImg = StringUtils.hasText(profileImg) ? profileImg : DEFAULT_PROFILE_IMG;

    }

    public void validatePermissionTo(Member target) {
        if (!(this.authority == Authority.ROLE_ADMIN || this.id.equals(target.id)))
            throw new IllegalArgumentException("Access denied: You do not have permission to perform this action");
    }

    public void changePassword(String currentPassword, String newPassword, PasswordEncoder encoder) {
        this.password = this.password.changePassword(currentPassword, newPassword, encoder);
    }

    //== validation ==//
    private void validateMemberInfo(String name, String email, LocalDate birthDate, Gender gender) {
        // name 검증
        if (!StringUtils.hasText(name)) throw new IllegalArgumentException("Name must not be blank");
        if (name.length() > 10) throw new IllegalArgumentException("Name must not exceed 10 characters");

        // email 검증
        if (!StringUtils.hasText(email)) throw new IllegalArgumentException("Email must not be blank");
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
            throw new IllegalArgumentException("Email format is invalid");

        // birthDate 검증
        if (birthDate == null) throw new IllegalArgumentException("Birth date is required");
        if (birthDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Birth date must be a date in the past");

        // gender 검증
        if (gender == null) throw new IllegalArgumentException("Gender is required");
    }

}
