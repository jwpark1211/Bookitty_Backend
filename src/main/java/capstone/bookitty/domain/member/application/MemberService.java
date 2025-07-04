package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Authority;
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
                null, request.gender(), request.birthdate(), Authority.ROLE_USER,passwordEncoder);

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

        Member target = findMemberById(memberId);
        Member current = findMemberByEmail(SecurityUtil.getCurrentMemberEmail());
        log.info("회원 삭제 권한 확인 - memberId: {}, currentEmail: {}", memberId, current.getEmail());

        if (!current.canDelete(target)) throw new IllegalArgumentException("삭제 권한이 없습니다.");

        memberRepository.delete(target);
        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }

    public MemberInfoResponse getMemberInfoWithId(Long memberId) {
        log.info("회원 정보 조회 요청 - memberId: {}", memberId);
        return MemberInfoResponse.from(findMemberById(memberId));
    }

    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        log.info("전체 회원 정보 조회 요청");
        return memberRepository.findAll(pageable)
                .map(MemberInfoResponse::from);
    }

    public MemberInfoResponse getMyInfo() {
        log.info("로그인 한 회원의 정보 조회 요청");

        String email = SecurityUtil.getCurrentMemberEmail();
        if (email == null) throw new UnauthenticatedMemberException();

        return memberRepository.findByEmail(email)
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new MemberNotFoundException());
    }

    //== private Method ==//
    private Member findMemberById(Long id) {
        if(id<=0) {
            log.warn("잘못된 회원 ID 요청 - id: {}", id);
            throw new IllegalArgumentException("잘못된 회원 ID입니다.");
        }
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthenticatedMemberException());
    }
}
