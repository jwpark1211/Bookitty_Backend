package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.api.dto.tokenDto.TokenRequest;
import capstone.bookitty.domain.member.api.dto.tokenDto.TokenResponse;
import capstone.bookitty.domain.member.application.authApplication.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "인증 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class AuthApi {

    private final AuthService authService;

    @Operation(summary = "로그인")
    @PostMapping(path = "/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid MemberLoginRequest request
    ) {

        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "토큰 재발행")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @RequestBody @Valid TokenRequest request
    ) {

        TokenResponse response = authService.reissue(request);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody TokenRequest tokenRequest) {

        authService.logout(tokenRequest);
        return ResponseEntity.noContent().build();

    }

}
