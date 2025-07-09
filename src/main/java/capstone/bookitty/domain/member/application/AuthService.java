package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.JwtToken;
import capstone.bookitty.global.authentication.JwtTokenProvider;
import capstone.bookitty.global.authentication.tokenDto.TokenRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
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
        Authentication authentication = authenticateUser(request.email(), request.password());
        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        refreshTokenService.save(authentication.getName(), jwtToken.refreshToken());
        Member member = getMemberByEmail(request.email());

        return TokenResponse.of(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    public TokenResponse reissue(TokenRequest request) {
        jwtTokenProvider.validateToken(request.refreshToken());

        Authentication authentication = jwtTokenProvider.getAuthentication(request.accessToken());
        refreshTokenService.validate(authentication.getName(), request.refreshToken());

        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        refreshTokenService.update(authentication.getName(), jwtToken.refreshToken());

        Member member = getMemberByEmail(authentication.getName());
        return TokenResponse.of(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    public void logout(TokenRequest request) {
        jwtTokenProvider.validateToken(request.refreshToken());

        Authentication authentication = jwtTokenProvider.getAuthentication(request.accessToken());
        refreshTokenService.delete(authentication.getName());

        tokenBlacklistService.blacklist(request.accessToken());
    }

    //== Private Methods ==//

    private Authentication authenticateUser(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }
}

