package capstone.bookitty.global.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final List<String> permitAllPaths;

    public JwtFilter(JwtTokenProvider jwtTokenProvider,
                     JwtProperties jwtProperties,
                     List<String> permitAllPaths) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.permitAllPaths = permitAllPaths;  // 동일 리스트 참조
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. permitAll 경로면 필터를 통과시킴
        if (isPermitAllPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. JWT 토큰 검사
        String jwt = resolveToken(request);
        log.debug("JWT Token: {}", jwt);

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

    private boolean isPermitAllPath(HttpServletRequest request) {
        return permitAllPaths.stream()
                .anyMatch(path -> new AntPathRequestMatcher(path).matches(request));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getAccessTokenHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getAuthType() + " ")) {
            return bearerToken.substring((jwtProperties.getAuthType() + " ").length()).trim();
        }
        return null;
    }
}
