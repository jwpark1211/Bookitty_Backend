package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public IdResponse saveMember(MemberSaveRequest request) {
        log.info("회원 저장 요청 - email: {}", request.email());

        if (memberRepository.existsByEmail(request.email())) {
            log.warn("회원 저장 실패 - 중복 이메일: {}", request.email());
            throw new DuplicateEmailException(request.email());
        }

        Password password = new Password(request.password());

        Member member = Member.create(request.name(),request.email(),password,
                null, request.gender(), request.birthdate(), passwordEncoder);

        memberRepository.save(member);
        log.info("회원 저장 완료 - memberId: {}, email: {}", member.getId(), member.getEmail());

        return IdResponse.of(member);
    }

    public BoolResponse isEmailUnique(String email) {
        log.info("이메일 중복 검사 요청 - email: {}", email);
        boolean isUnique = !memberRepository.existsByEmail(email);
        log.info("이메일 중복 검사 결과 - email: {}, isUnique: {}", email, isUnique);
        return BoolResponse.of(isUnique);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        log.info("회원 삭제 요청 - memberId: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        memberRepository.delete(member);
        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }

    public MemberInfoResponse getMemberInfoWithId(Long memberId) {
        log.info("회원 정보 조회 요청 - memberId: {}", memberId);
        return memberRepository.findById(memberId)
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        log.info("전체 회원 정보 조회 요청");
        return memberRepository.findAll(pageable)
                .map(MemberInfoResponse::from);
    }

    public MemberInfoResponse getMyInfo(){
        log.info("로그인 한 회원의 정보 조회 요청");
        return memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new UnauthenticatedMemberException());
    }
}
