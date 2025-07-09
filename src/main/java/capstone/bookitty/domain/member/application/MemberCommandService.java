package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.application.factory.MemberFactory;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final MemberFactory memberFactory;

    @Transactional
    public Long saveMember(MemberSaveRequest request) {
        Member member = memberFactory.create(request);
        memberRepository.save(member);
        return member.getId();
    }

    @Transactional
    public void deleteMember(Long id) {
        Member target = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));

        String email = SecurityUtil.getCurrentMemberEmail();
        if (email == null) throw new UnauthenticatedMemberException();
        Member current = memberRepository.findByEmail(email).orElseThrow(() -> new UnauthenticatedMemberException(email));

        current.validatePermissionTo(target);
        memberRepository.delete(target);
    }

}
