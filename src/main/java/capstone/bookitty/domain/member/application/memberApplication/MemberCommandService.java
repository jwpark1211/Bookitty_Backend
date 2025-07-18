package capstone.bookitty.domain.member.application.memberApplication;

import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.PasswordEncoder;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long saveMember(MemberSaveRequest request) {
        validateEmailUniqueness(request.email());

        Member member = Member.builder()
                .name(request.name())
                .email(request.email())
                .password(Password.fromRaw(request.password(), passwordEncoder))
                .gender(request.gender())
                .birthDate(request.birthdate())
                .build();

        member = memberRepository.save(member);
        return member.getId();
    }

    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        String email = SecurityUtil.getCurrentMemberEmail();
        if (email == null) throw new UnauthenticatedMemberException();
        Member current = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthenticatedMemberException(email));

        current.validatePermissionTo(member);

        memberRepository.delete(member);
    }

    //== private methods ==//

    private void validateEmailUniqueness(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

}
