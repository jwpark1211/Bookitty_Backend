package capstone.bookitty.domain.dto.tokenDto;

import capstone.bookitty.jwt.JwtToken;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;


public record TokenResponse(
    Long idx,
    JwtToken jwtToken,
    String profileImg,
    String name
){
    public static TokenResponse of(Long idx, JwtToken jwtToken, String profileImg, String name){
        return new TokenResponse(idx,jwtToken,profileImg,name);
    }
}