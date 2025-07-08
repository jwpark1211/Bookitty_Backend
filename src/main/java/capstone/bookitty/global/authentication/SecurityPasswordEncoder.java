package capstone.bookitty.global.authentication;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Primary
@Component
public class SecurityPasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder delegate;

    public SecurityPasswordEncoder() {
        this.delegate = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
