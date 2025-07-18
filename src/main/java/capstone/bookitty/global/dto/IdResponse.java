package capstone.bookitty.global.dto;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.star.domain.Star;

public record IdResponse(Long id) {

    public static IdResponse of(Star star) {
        return new IdResponse(star.getId());
    }

    public static IdResponse of(Member member) {
        return new IdResponse(member.getId());
    }

}
