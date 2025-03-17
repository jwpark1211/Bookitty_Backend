package capstone.bookitty.domain.member.api;

import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
import capstone.bookitty.domain.member.application.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "member", description = "회원 관련 api 입니다.")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberApi {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<IdResponse> createMember(
            @RequestBody @Valid MemberSaveRequest request){

        log.info("회원가입 요청 - email: {}", request.email());
        IdResponse response = memberService.saveMember(request);

        log.info("회원가입 완료 - 회원 ID: {}", response.id());
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/email/unique")
    public ResponseEntity<BoolResponse> isEmailUnique(
            @RequestParam("email") String email
    ){
        log.info("이메일 중복 확인 요청 - email: {}", email);
        BoolResponse response = memberService.isEmailUnique(email);

        log.info("이메일 중복 확인 완료 - 결과 : {}", response);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인")
    @PostMapping(path = "/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid MemberLoginRequest request
    ){
        log.info("로그인 요청 - email: {}", request.email());
        TokenResponse response = memberService.login(request);

        log.info("로그인 완료 - 회원 ID: {}, 이름: {}", response.idx(), response.name());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 재발행")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @RequestBody @Valid TokenRequest request
    ){
        log.info("토큰 재발행 요청");
        TokenResponse response = memberService.reissue(request);

        log.info("토큰 재발행 완료");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인 한 회원 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMyMemberInfo() {
        try {
            log.info("현재 로그인 한 회원 정보 조회 요청");
            MemberInfoResponse response = memberService.getMyInfo();

            log.info("회원 정보 조회 완료 - memberId: {}, name: {}", response.id(), response.name());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회원 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRequest tokenRequest) {
        log.info("로그아웃 요청");
        memberService.logout(tokenRequest);

        log.info("로그아웃 완료");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "id로 회원 조회")
    @GetMapping(path = "/{member-id}")
    public ResponseEntity<MemberInfoResponse> findOneMember(
            @PathVariable("member-id") Long memberId
    ){
        log.info("회원 정보 조회 요청 - memberId: {}", memberId);
        MemberInfoResponse response = memberService.getMemberInfoWithId(memberId);

        log.info("회원 정보 조회 완료 - memberId: {}, name: {}", memberId, response.name());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 회원 조회")
    @GetMapping
    public ResponseEntity<Page<MemberInfoResponse>> findAllMembers(
            @PageableDefault(sort="id",size = 10) Pageable pageable
    ){
        log.info("전체 회원 목록 조회 요청 - 페이지 정보: {}", pageable);
        Page<MemberInfoResponse> responseList = memberService.getAllMemberInfo(pageable);

        log.info("전체 회원 목록 조회 완료 - 총 회원 수: {}", responseList.getTotalElements());
        return ResponseEntity.ok(responseList);
    }

    /*@Operation(summary = "회원 프로필 업로드 / requestPart 이름 : profile")
    @PutMapping(path = "/{member-id}/profile")
    public ResponseEntity<MemberInfoResponse> updateMemberProfile(
            @PathVariable("member-id") Long memberId,
            @RequestPart(value = "profile") MultipartFile profileImg
            ) throws IOException {
        MemberInfoResponse response = memberService.updateProfile(memberId, profileImg);
        return ResponseEntity.ok(response);
    }*/

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping(path = "/{member-id}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("member-id") Long memberId
    ){
        log.info("회원 탈퇴 요청 - memberId: {}", memberId);
        memberService.deleteMember(memberId);

        log.info("회원 탈퇴 완료 - memberId: {}", memberId);
        return ResponseEntity.noContent().build();
    }

}
