package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.application.memberApplication.MemberQueryService;
import capstone.bookitty.global.dto.BoolResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class MemberQueryApi {

    private final MemberQueryService memberQueryService;

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

}
