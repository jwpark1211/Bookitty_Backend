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
        Member target = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        Member current = memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .orElseThrow(() -> new UnauthenticatedMemberException());
        if (!current.canDelete(target)) throw new IllegalArgumentException("삭제 권한이 없습니다.");

        memberRepository.delete(target);
    }

}
