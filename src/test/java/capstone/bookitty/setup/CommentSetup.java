package capstone.bookitty.setup;

import capstone.bookitty.domain.comment.domain.Comment;
import capstone.bookitty.domain.comment.domain.Like;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.comment.repository.CommentRepository;
import capstone.bookitty.domain.comment.repository.LikeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class CommentSetup {
    private final MemberSetup memberSetup;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public Comment save(){
        final Member member = memberSetup.save();
        final Comment comment = buildComment(member,"isbn","content");
        return commentRepository.save(comment);
    }

    public Like saveLike(){
        final Comment comment = save();
        final Member member = memberSetup.save();
        final Like like = new Like(member,comment);
        return likeRepository.save(like);
    }

    public List<Comment> save(int count){
        List<Comment> comments = new ArrayList<>();
        List<Member> members = memberSetup.save(count);
        IntStream.range(0,count).forEach(i->comments.add(
                commentRepository.save(buildComment(
                        members.get(i),String.format("isbn%d",i),String.format("comment%d",i)))));
        return comments;
    }

    public List<Comment> save_oneIsbnManyComments(int count){
        List<Comment> comments = new ArrayList<>();
        List<Member> members = memberSetup.save(count);
        IntStream.range(0,count).forEach(i->comments.add(
                commentRepository.save(buildComment(
                        members.get(i),"isbn",String.format("comment%d",i)))));
        return comments;
    }

    public List<Comment> save_oneMemberManyComments(int count){
        List<Comment> comments = new ArrayList<>();
        final Member member = memberSetup.save();
        IntStream.range(0,count).forEach(i->comments.add(
                commentRepository.save(buildComment(
                        member,String.format("isbn%d",i),String.format("comment%d",i)))));
        return comments;
    }

    private Comment buildComment(Member member, String isbn, String content){
        return Comment.builder()
                .member(member)
                .isbn(isbn)
                .content(content)
                .build();
    }
}
