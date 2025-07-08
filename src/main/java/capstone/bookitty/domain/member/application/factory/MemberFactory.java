package capstone.bookitty.domain.member.application.factory;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberFactory {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public Member create(MemberSaveRequest request) {
        validateEmailUniqueness(request.email());

        Password password = new Password(request.password());
        String encodedPassword = passwordEncoder.encode(password.getRaw());

        return Member.createUser(
                request.name(),
                request.email(),
                encodedPassword,
                request.gender(),
                request.birthdate()
        );
    }

    private void validateEmailUniqueness(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}

