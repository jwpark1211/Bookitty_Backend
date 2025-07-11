package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.api.dto.tokenDto.TokenRequest;
import capstone.bookitty.domain.member.api.dto.tokenDto.TokenResponse;
import capstone.bookitty.domain.member.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "인증 관련 api 입니다.")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthApi {

    private final AuthService authService;

    @Operation(summary = "로그인")
    @PostMapping(path = "/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid MemberLoginRequest request
    ) {
        log.info("로그인 요청 - email: {}", request.email());
        TokenResponse response = authService.login(request);

        log.info("로그인 완료 - 회원 ID: {}, 이름: {}", response.idx(), response.name());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 재발행")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @RequestBody @Valid TokenRequest request
    ) {
        log.info("토큰 재발행 요청");
        TokenResponse response = authService.reissue(request);

        log.info("토큰 재발행 완료");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRequest tokenRequest) {
        log.info("로그아웃 요청");
        authService.logout(tokenRequest);

        log.info("로그아웃 완료");
        return ResponseEntity.noContent().build();
    }

}
