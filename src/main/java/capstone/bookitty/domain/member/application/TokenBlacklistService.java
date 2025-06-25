package capstone.bookitty.domain.member.application;

import capstone.bookitty.global.authentication.JwtTokenProvider;
import capstone.bookitty.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisUtil redisUtil;
    private final JwtTokenProvider jwtTokenProvider;

    public void blacklist(String accessToken) {
        long exp = jwtTokenProvider.getExpiration(accessToken);
        redisUtil.setBlackList(accessToken, "access_token", exp);
    }
}

