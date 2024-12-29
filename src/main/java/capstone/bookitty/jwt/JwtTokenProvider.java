package capstone.bookitty.jwt;

import capstone.bookitty.common.CustomUserDetailsService;
import capstone.bookitty.util.RedisUtil;
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
                            CustomUserDetailsService customUserDetailsService,RedisUtil redisUtil) {
        this.jwtProperties = jwtProperties;
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
    }

    public Long getExpiration(String accessToken) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    public JwtToken generateTokenDto(Authentication authentication) {
        // 권한
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // access token 생성
        Date accessTokenExpiresIn = new Date(now + jwtProperties.getAccessTokenExpire());
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(jwtProperties.getAuthoritiesKey(), authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        //refresh token 생성
        Date refreshTokenExpiresIn = new Date(now + jwtProperties.getRefreshTokenExpire());
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.of(jwtProperties.getAuthType(),accessToken,refreshToken);
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(jwtProperties.getAuthoritiesKey()) == null) {
            throw new RuntimeException("Token with no permissions information");
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(jwtProperties.getAuthoritiesKey()).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            if(redisUtil.hasKeyBlackList(token)) {
                return false;
            }
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}