package capstone.bookitty.domain.member.fixture;

import capstone.bookitty.domain.member.api.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.domain.type.Gender;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.global.authentication.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MemberTestFixture {
    private static String email = "default@email.com";
    private static String password = "Valid123!";
    private static String name = "홍길동";
    private static LocalDate birthDate = LocalDate.of(2001, 12, 11);

    private final PasswordEncoder passwordEncoder;

    public Password createPassword(String rawPassword) {
        return Password.fromRaw(rawPassword, passwordEncoder);
    }

    public MemberSaveRequest.MemberSaveRequestBuilder createMemberSaveRequest() {
        return MemberSaveRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1999, 1, 1));
    }

    public Member.MemberBuilder createMember() {
        return Member.builder()
                .email(email)
                .name(name)
                .birthDate(birthDate)
                .password(createPassword(password))
                .authority(Authority.ROLE_USER)
                .gender(Gender.FEMALE);
    }

    public MemberLoginRequest.MemberLoginRequestBuilder createMemberLoginRequest() {
        return MemberLoginRequest.builder()
                .email(email)
                .password(password);
    }
}
