package capstone.bookitty.domain.member.domain.factory;

import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberFactory {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member create(MemberSaveRequest request) {
        validateEmailUniqueness(request.email());

        return Member.builder()
                .name(request.name())
                .email(request.email())
                .password(Password.fromRaw(request.password(), passwordEncoder))
                .gender(request.gender())
                .birthDate(request.birthdate())
                .build();
    }

    private void validateEmailUniqueness(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}

