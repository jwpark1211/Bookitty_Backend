package capstone.bookitty.domain.dto.commonDto;

import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Comment;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.Star;

public record IdResponse (Long id){
    public static IdResponse of(Long id){ return new IdResponse(id); }
    public static IdResponse of(Star star){
        return new IdResponse(star.getId());
    }
    public static IdResponse of(Member member) { return new IdResponse(member.getId()); }
    public static IdResponse of(BookState bookState) { return new IdResponse(bookState.getId()); }
    public static IdResponse of(Comment comment){ return new IdResponse(comment.getId()); }
}
