package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.application.MemberCommandService;
import capstone.bookitty.domain.member.application.MemberQueryService;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.dto.IdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "member", description = "회원 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberApi {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;


    @Operation(summary = "회원가입")
    @PostMapping("/new")
    public ResponseEntity<IdResponse> createMember(
            @RequestBody @Valid MemberSaveRequest request) {
        IdResponse response = new IdResponse(memberCommandService.saveMember(request));
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/email/unique")
    public ResponseEntity<BoolResponse> isEmailUnique(
            @Email @NotBlank @RequestParam("email") String email
    ) {
        BoolResponse response = new BoolResponse(memberQueryService.isEmailUnique(email));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인 한 회원 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMyMemberInfo() {
        MemberInfoResponse response = memberQueryService.getMyInfo();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "id로 회원 조회")
    @GetMapping(path = "/{member-id}")
    public ResponseEntity<MemberInfoResponse> findOneMember(
            @PathVariable("member-id") Long memberId
    ) {
        MemberInfoResponse response = memberQueryService.getMemberInfoWithId(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 회원 조회")
    @GetMapping
    public ResponseEntity<Page<MemberInfoResponse>> findAllMembers(
            @PageableDefault(sort = "id", size = 10) Pageable pageable
    ) {
        Page<MemberInfoResponse> responseList = memberQueryService.getAllMemberInfo(pageable);
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping(path = "/{member-id}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("member-id") Long memberId
    ) {
        memberCommandService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

}
