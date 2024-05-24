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

    public TokenResponseDTO(Long idx, JwtToken jwtToken) {
        this.idx = idx;
        this.jwtToken = jwtToken;
    }
}