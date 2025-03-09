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

@Tag(name = "회원", description = "회원 관련 api 입니다.")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberApi {

    private final MemberService memberService;

    @Operation(summary = "회원가입/"+"비밀번호=영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자/"
    +"gender=MALE or FEMALE/"+"birthdate=yyyy-mm-dd/"+"email=이메일 format 준수")
    @PostMapping(path = "/new")
    public ResponseEntity<IdResponse> save(
            @RequestBody @Valid MemberSaveRequest request){
        IdResponse response = memberService.saveMember(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping(path = "/email/{email}/unique")
    public ResponseEntity<BoolResponse> isEmailUnique(
            @PathVariable("email") String email
    ){
        BoolResponse response = memberService.isEmailUnique(email);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "로그인")
    @PostMapping(path = "/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid MemberLoginRequest request
    ){
        TokenResponse response = memberService.login(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "토큰 재발행")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @RequestBody @Valid TokenRequest request
    ){
        TokenResponse response = memberService.reissue(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "로그인 한 회원 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMyMemberInfo(){
        log.info("api entry");
        MemberInfoResponse response = memberService.getMyInfo();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "로그아웃") /* 삭제 고민*/
    @PostMapping("/logout")
    public void logout(@RequestBody TokenRequest tokenRequest) {
        memberService.logout(tokenRequest);
    }

    @Operation(summary = "id로 회원 조회")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MemberInfoResponse> findOneMember(
            @PathVariable("id") Long memberId
    ){
        MemberInfoResponse response = memberService.getMemberInfoWithId(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "전체 회원 조회 / page는 requestParam으로 요청할 수 있습니다. / "+
    "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/all")
    public ResponseEntity<Page<MemberInfoResponse>> findAllMembers(
            @PageableDefault(sort="id",size = 10) Pageable pageable
    ){
        Page<MemberInfoResponse> responseList = memberService.getAllMemberInfo(pageable);
        return ResponseEntity.ok().body(responseList);
    }

    /*@Operation(summary = "회원 프로필 업로드 / requestPart 이름 : profile")
    @PostMapping(path = "/{id}/profile")
    public ResponseEntity<MemberInfoResponse> updateMemberProfile(
            @PathVariable("id") Long memberId,
            @RequestPart(value = "profile") MultipartFile profileImg
            ) throws IOException {
        MemberInfoResponse response = memberService.updateProfile(memberId, profileImg);
        return ResponseEntity.ok().body(response);
    }*/

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping(path = "/{id}")
    public void deleteMember(
            @PathVariable("id") Long memberId
    ){
        memberService.deleteMember(memberId);
    }

}
