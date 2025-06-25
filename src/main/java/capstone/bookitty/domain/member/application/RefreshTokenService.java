package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.exception.InvalidRefreshTokenException;
import capstone.bookitty.domain.member.exception.NotLoggedInException;
import capstone.bookitty.domain.member.repository.RefreshTokenRepository;

import capstone.bookitty.global.authentication.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    public void save(String key, String value) {
        repository.save(new RefreshToken(key, value));
    }

    public void validate(String key, String inputToken) {
        RefreshToken token = repository.findByKey(key)
                .orElseThrow(() -> new NotLoggedInException());
        if (!token.getValue().equals(inputToken)) {
            throw new InvalidRefreshTokenException();
        }
    }

    public void update(String key, String newValue) {
        RefreshToken token = repository.findByKey(key)
                .orElseThrow(() -> new NotLoggedInException());
        token.updateValue(newValue);
        repository.save(token);
    }

    public void delete(String key) {
        repository.findByKey(key).ifPresent(repository::delete);
    }
}
