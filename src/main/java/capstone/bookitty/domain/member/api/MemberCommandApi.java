package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.application.memberApplication.MemberCommandService;
import capstone.bookitty.global.dto.IdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "member", description = "회원 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberCommandApi {

    private final MemberCommandService memberCommandService;

    @Operation(summary = "회원가입")
    @PostMapping("/new")
    public ResponseEntity<IdResponse> createMember(
            @RequestBody @Valid MemberSaveRequest request) {

        IdResponse response = new IdResponse(memberCommandService.saveMember(request));
        return ResponseEntity.status(201).body(response);

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
