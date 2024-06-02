package capstone.bookitty.setup;

import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Component
public class MemberSetup {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member save() {
        final Member member = buildMember("woon@test.com","woon");
        return memberRepository.save(member);
    }

    public List<Member> save(int count) {
        List<Member> members = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> members.add(
                memberRepository.save(buildMember(String.format("woon00%d@test.com", i),
                                                  String.format("woon00%d", i)))));
        return members;
    }

    private Member buildMember(String email,String name) {
        return Member.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(getDefaultPassword()))
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000,3,8))
                .build();
    }

    public String getDefaultPassword(){ return "Password!12"; }

}
