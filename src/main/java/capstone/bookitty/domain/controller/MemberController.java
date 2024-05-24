package capstone.bookitty.domain.controller;

import capstone.bookitty.domain.dto.ResponseType.BasicResponse;
import capstone.bookitty.domain.dto.ResponseType.ResponseCounter;
import capstone.bookitty.domain.dto.ResponseType.ResponseString;
import capstone.bookitty.domain.dto.TokenResponseDTO;
import capstone.bookitty.domain.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static capstone.bookitty.domain.dto.MemberDTO.*;

@Tag(name = "회원", description = "회원 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "deploy test")
    @GetMapping("/test")
    public String test(){
        return "DEPLOY 0517";
    }

    @Operation(summary = "회원가입/"+"비밀번호=영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자/"
    +"gender=MALE or FEMALE/"+"birthdate=yyyy-mm-dd/"+"email=이메일 format 준수")
    @PostMapping(path = "/new")
    public ResponseEntity<? extends BasicResponse> save(
            @RequestBody @Valid MemberSaveRequest request){
        return ResponseEntity.ok()
                .body(new ResponseCounter<IdResponse>(
                       memberService.saveMember(request)));
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping(path = "/email/{email}/unique")
    public ResponseEntity<? extends BasicResponse> isEmailUnique(
            @PathVariable("email") String email
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<BoolResponse>(
                        memberService.isEmailUnique(email)));
    }

    @Operation(summary = "로그인")
    @PostMapping(path = "/login")
    public ResponseEntity<? extends BasicResponse> login(
            @RequestBody @Valid MemberLoginRequest request
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<TokenResponseDTO>(memberService.login(request)));
    }

    @Operation(summary = "id로 회원 조회")
    @GetMapping(path = "/{id}")
    public ResponseEntity<? extends BasicResponse> findOneMember(
            @PathVariable("id") Long memberId
    ){
        return ResponseEntity.ok(
                new ResponseCounter<MemberInfoResponse>(
                        memberService.getMemberInfoWithId(memberId)));
    }

    @Operation(summary = "전체 회원 조회 / page는 requestParam으로 요청할 수 있습니다. / "+
    "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/all")
    public ResponseEntity<? extends BasicResponse> findAllMembers(
            @PageableDefault(sort="id",size = 10) Pageable pageable
    ){
        return ResponseEntity.ok(
                new ResponseCounter<Page<MemberInfoResponse>>(
                        memberService.getAllMemberInfo(pageable)));
    }

    @Operation(summary = "회원 프로필 업로드 / requestPart 이름 : profile")
    @PostMapping(path = "/{id}/profile")
    public ResponseEntity<? extends BasicResponse> updateMemberProfile(
            @PathVariable("id") Long memberId,
            @RequestPart(value = "profile") MultipartFile profileImg
            ) throws IOException {
        memberService.updateProfile(memberId, profileImg);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<? extends BasicResponse> deleteMember(
            @PathVariable("id") Long memberId
    ){
        memberService.deleteMember(memberId);
        return ResponseEntity.ok()
                .body(new ResponseString("delete member!"));
    }

}
