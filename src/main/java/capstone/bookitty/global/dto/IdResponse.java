package capstone.bookitty.global.dto;

import capstone.bookitty.domain.comment.domain.Comment;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.star.domain.Star;

public record IdResponse(Long id) {
    public static IdResponse of(Long id) {
        return new IdResponse(id);
    }

    public static IdResponse of(Star star) {
        return new IdResponse(star.getId());
    }

    public static IdResponse of(Member member) {
        return new IdResponse(member.getId());
    }

    public static IdResponse of(Comment comment) {
        return new IdResponse(comment.getId());
    }
}
