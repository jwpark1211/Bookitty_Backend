package capstone.bookitty.global.authentication.tokenDto;

import capstone.bookitty.global.authentication.JwtToken;


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