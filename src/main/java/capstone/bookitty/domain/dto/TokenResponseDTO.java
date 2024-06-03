package capstone.bookitty.domain.dto;

import capstone.bookitty.jwt.JwtToken;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class TokenResponseDTO {
    private Long idx;
    private JwtToken jwtToken;
    private String profileImg;
    private String name;

    public TokenResponseDTO(Long idx, JwtToken jwtToken,String profileImg,String name) {
        this.idx = idx;
        this.jwtToken = jwtToken;
        this.profileImg = profileImg;
        this.name = name;
    }
}