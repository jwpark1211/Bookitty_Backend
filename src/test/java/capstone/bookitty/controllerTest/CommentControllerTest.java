package capstone.bookitty.controllerTest;

import capstone.bookitty.IntergrationTest;

import capstone.bookitty.domain.entity.Comment;
import capstone.bookitty.domain.entity.Like;
import capstone.bookitty.domain.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static capstone.bookitty.domain.dto.CommentDTO.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTest extends IntergrationTest {

    @Test
    public void 아이디로개별조회() throws Exception{
        //given
        Comment comment = commentSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(get("/comment/{comment-id}",comment.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(comment.getId()))
                .andExpect(jsonPath("$.data.memberId").value(comment.getMember().getId()))
                .andExpect(jsonPath("$.data.memberName").value(comment.getMember().getName()))
                .andExpect(jsonPath("$.data.memberProfileImg").value(comment.getMember().getProfileImg()))
                .andExpect(jsonPath("$.data.isbn").value(comment.getIsbn()))
                .andExpect(jsonPath("$.data.content").value(comment.getContent()));
    }

    @Test
    public void 아이디로개별조회_실패() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(get("/comment/{comment-id}",0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Comment with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 코멘트생성() throws Exception{
        //given
        final Member member = memberSetup.save();
        final CommentSaveRequest request = CommentSaveRequest.buildForTest("isbn",member.getId(),"content");

        //when
        final ResultActions resultActions = mvc.perform(post("/comment/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 코멘트생성_멤버없음() throws Exception{
        //given
        final CommentSaveRequest request = CommentSaveRequest.buildForTest("isbn",0L,"content");

        //when
        final ResultActions resultActions = mvc.perform(post("/comment/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Member with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 전체코멘트정보() throws Exception{
        //given
        List<Comment> comments = commentSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/comment/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void isbn으로코멘트정보() throws Exception{
        //given
        List<Comment> comments = commentSetup.save_oneIsbnManyComments(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/comment/isbn/{isbn}",comments.get(1).getIsbn())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void memberId로코멘트정보() throws Exception{
        //given
        List<Comment> comments = commentSetup.save_oneMemberManyComments(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/comment/member/{member-id}",comments.get(1).getMember().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void 코멘트수정() throws Exception{
        //given
        Comment comment = commentSetup.save();
        CommentUpdateRequest request = CommentUpdateRequest.buildForTest("modify");

        //when
        final ResultActions resultActions = mvc.perform(patch("/comment/{comment-id}",comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 코멘트수정_코멘트없음() throws Exception{
        //given
        CommentUpdateRequest request = CommentUpdateRequest.buildForTest("modify");

        //when
        final ResultActions resultActions = mvc.perform(patch("/comment/{comment-id}",0L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Comment with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 코멘트삭제() throws Exception{
        //given
        Comment comment = commentSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(delete("/comment/{comment-id}",comment.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 코멘트삭제_코멘트없음() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(delete("/comment/{comment-id}",0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Comment with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 코멘트좋아요등록() throws Exception{
        //given
        Member member = memberSetup.save();
        Comment comment = commentSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(post(
                "/comment/{comment-id}/member/{member-id}/like/increase",comment.getId(),member.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 코멘트좋아요등록_회원정보없음() throws Exception{
        //given
        Comment comment = commentSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(post(
                "/comment/{comment-id}/member/{member-id}/like/increase",comment.getId(),0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Member with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 코멘트좋아요삭제() throws Exception{
        //given
       Like like = commentSetup.saveLike();

        //when
        final ResultActions resultActions = mvc.perform(post(
                        "/comment/{comment-id}/member/{member-id}/like/decrease",
                        like.getComment().getId(),like.getMember().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 코멘트좋아요삭제_회원정보없음() throws Exception{
        //given
        Like like = commentSetup.saveLike();

        //when
        final ResultActions resultActions = mvc.perform(post(
                        "/comment/{comment-id}/member/{member-id}/like/decrease",
                        like.getComment().getId(),0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Member with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }
}
