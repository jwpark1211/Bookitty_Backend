package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {
    private final MemberRepository memberRepository;

    public MemberInfoResponse getMemberInfoWithId(Long id) {
        return MemberInfoResponse.from(memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id)));
    }

    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberInfoResponse::from);
    }

    public MemberInfoResponse getMyInfo() {
        String email = SecurityUtil.getCurrentMemberEmail();
        if (email == null) throw new UnauthenticatedMemberException();

        return memberRepository.findByEmail(email)
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    public BoolResponse isEmailUnique(String email) {
        boolean isUnique = !memberRepository.existsByEmail(email);
        return BoolResponse.of(isUnique);
    }
}
