package capstone.bookitty.util;

import capstone.bookitty.common.CustomUserDetails;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {
    public static String getCurrentMemberEmail() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("No authentication information in Security Context");
            throw new RuntimeException("Security Context does not contain credentials.");
        }

        log.info("Authentication details: {}", authentication);

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            log.warn("Authentication principal is not an instance of CustomUserDetails. Principal: {}", authentication.getPrincipal());
            throw new RuntimeException("Security Context does not contain proper credentials.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Authenticated user Email: {}", userDetails.getUsername());
        return userDetails.getUsername();
    }

}