package capstone.bookitty.domain.member.fixture;

import capstone.bookitty.domain.member.domain.Authority;
import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MemberTestFixture {
    private static String email = "default@email.com";
    private static String password = "Valid123!";
    private static String name = "홍길동";
    private static String encodedPassword = "encodedPassword";
    private static LocalDate birthDate = LocalDate.of(2001,12,11);

    public MemberSaveRequest.MemberSaveRequestBuilder createMemberSaveRequest() {
        return MemberSaveRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1999, 1, 1));
    }

    public Member.MemberBuilder createMember(){
        return Member.builder()
                .email(email)
                .name(name)
                .birthDate(birthDate)
                .encodedPassword(encodedPassword)
                .authority(Authority.ROLE_USER)
                .gender(Gender.FEMALE);
    }

    public MemberLoginRequest.MemberLoginRequestBuilder createMemberLoginRequest() {
        return MemberLoginRequest.builder()
                .email(email)
                .password(password);
    }
}
