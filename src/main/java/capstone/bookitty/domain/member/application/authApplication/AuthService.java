package capstone.bookitty.domain.member.application.authApplication;

import capstone.bookitty.domain.member.api.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.api.dto.tokenDto.TokenRequest;
import capstone.bookitty.domain.member.api.dto.tokenDto.TokenResponse;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.JwtToken;
import capstone.bookitty.global.authentication.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public TokenResponse login(MemberLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        refreshTokenService.save(authentication.getName(), jwtToken.refreshToken());
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new MemberNotFoundException(request.email()));

        return TokenResponse.of(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    public TokenResponse reissue(TokenRequest request) {
        jwtTokenProvider.validateToken(request.refreshToken());

        Authentication authentication = jwtTokenProvider.getAuthentication(request.accessToken());
        refreshTokenService.validate(authentication.getName(), request.refreshToken());

        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        refreshTokenService.update(authentication.getName(), jwtToken.refreshToken());

        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberNotFoundException(authentication.getName()));
        return TokenResponse.of(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    public void logout(TokenRequest request) {
        jwtTokenProvider.validateToken(request.refreshToken());

        Authentication authentication = jwtTokenProvider.getAuthentication(request.accessToken());
        refreshTokenService.delete(authentication.getName());

        tokenBlacklistService.blacklist(request.accessToken());
    }

}

