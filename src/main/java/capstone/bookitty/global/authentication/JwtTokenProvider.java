package capstone.bookitty.global.authentication;

import capstone.bookitty.global.util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final RedisUtil redisUtil;
    private final Key key;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, JwtProperties jwtProperties,
                            CustomUserDetailsService customUserDetailsService, RedisUtil redisUtil) {
        this.jwtProperties = jwtProperties;
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
    }

    public Long getExpiration(String accessToken) {
        log.debug("AccessToken 만료 시간 조회 요청: {}", accessToken);
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        Long now = new Date().getTime();
        Long remainingTime = expiration.getTime() - now;
        log.debug("AccessToken 남은 유효 시간(ms): {}", remainingTime);
        return remainingTime;
    }

    public JwtToken generateTokenDto(Authentication authentication) {
        log.debug("JWT 토큰 생성 시작 - 사용자: {}", authentication.getName());

        // 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        log.debug("사용자 권한: {}", authorities);

        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + jwtProperties.getAccessTokenExpire());
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(jwtProperties.getAuthoritiesKey(), authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        log.debug("AccessToken 생성 완료 - 만료 시간: {}", accessTokenExpiresIn);

        // Refresh Token 생성
        Date refreshTokenExpiresIn = new Date(now + jwtProperties.getRefreshTokenExpire());
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        log.debug("RefreshToken 생성 완료 - 만료 시간: {}", refreshTokenExpiresIn);

        return JwtToken.of(jwtProperties.getAuthType(), accessToken, refreshToken);
    }

    public Authentication getAuthentication(String accessToken) {
        log.debug("AccessToken을 이용한 사용자 인증 요청: {}", accessToken);

        Claims claims = parseClaims(accessToken);
        if (claims.get(jwtProperties.getAuthoritiesKey()) == null) {
            log.warn("토큰에 권한 정보 없음");
            throw new RuntimeException("Token with no permissions information");
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(jwtProperties.getAuthoritiesKey()).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        log.debug("토큰에서 추출된 사용자 권한: {}", authorities);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());
        log.debug("인증된 사용자 정보: {}", userDetails.getUsername());

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public boolean validateToken(String token) {
        log.debug("JWT 토큰 검증 요청: {}", token);
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            if (redisUtil.hasKeyBlackList(token)) {
                log.debug("블랙리스트에 등록된 토큰: {}", token);
                return false;
            }
            log.debug("토큰 검증 성공: {}", token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", token, e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", token, e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", token, e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT Claims가 비어 있음: {}", token, e);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            log.debug("AccessToken 파싱 중: {}", accessToken);
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            log.warn("만료된 AccessToken - Claims 반환: {}", accessToken);
            return e.getClaims();
        }
    }
}
