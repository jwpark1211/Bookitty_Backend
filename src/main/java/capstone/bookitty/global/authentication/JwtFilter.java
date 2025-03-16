package capstone.bookitty.global.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public JwtFilter(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 요청에서 JWT 토큰 추출
        String jwt = resolveToken(request);
        log.debug("JWT Token: {}", jwt);

        // 2. JWT 유효성 검사 및 인증 처리
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication set in SecurityContextHolder: {}", authentication);
        } else if (jwt != null) {
            log.warn("Invalid JWT token: {}", jwt);
        }

        // 3. 필터 체인 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 요청의 Authorization 헤더에서 JWT 토큰을 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getAccessTokenHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getAuthType() + " ")) {
            return bearerToken.substring((jwtProperties.getAuthType() + " ").length()).trim();
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        List<String> excludeUrls = List.of("/books/**", "/members/login", "/members/reissue",
                "/members/email/unique", "/swagger-ui/**", "/actuator/**");

        return excludeUrls.stream().anyMatch(url -> new AntPathRequestMatcher(url).matches(request));
    }

}
