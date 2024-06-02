package capstone.bookitty.controllerTest;

import capstone.bookitty.IntergrationTest;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.Star;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static capstone.bookitty.domain.dto.StarDTO.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StarControllerTest extends IntergrationTest {

    @Test
    public void 아이디로개별조회() throws Exception{
        //given
        Star star = starSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(get("/star/{star-id}",star.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(star.getId()))
                .andExpect(jsonPath("$.data.memberId").value(star.getMember().getId()))
                .andExpect(jsonPath("$.data.isbn").value(star.getIsbn()))
                .andExpect(jsonPath("$.data.score").value(star.getScore()));
    }

    @Test
    public void 아이디로개별조회_실패() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(get("/star/{star-id}",0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Star with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 평점생성() throws Exception{
        //given
        final Member member = memberSetup.save();
        final StarSaveRequest request = StarSaveRequest.buildForTest("isbn",member.getId(),4);

        //when
        final ResultActions resultActions = mvc.perform(post("/star/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void 평점생성_valid오류_회원없음() throws Exception{
        //given
        final StarSaveRequest request = StarSaveRequest.buildForTest("isbn",0L,4);

        //when
        final ResultActions resultActions = mvc.perform(post("/star/new")
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
    public void 전체평점정보() throws Exception{
        //given
        List<Star> stars = starSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/star/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void isbn으로평점정보() throws Exception{
        //given
        List<Star> stars = starSetup.save_oneIsbnManyStars(4);

        //when
        final ResultActions resultActions = mvc.perform(get("/star/isbn/{isbn}",stars.get(1).getIsbn())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //when
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4));

    }

    @Test
    public void memberId로평점정보() throws Exception{
        //given
        List<Star> stars = starSetup.save_oneMemberManyStars(4);

        //when
        final ResultActions resultActions = mvc.perform(get("/star/member/{member-id}",stars.get(1).getMember().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //when
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4));

    }

    @Test
    public void 평점수정() throws Exception{
        //given
        Star star = starSetup.save();
        StarUpdateRequest request = StarUpdateRequest.buildForTest(3);

        //when
        final ResultActions resultActions = mvc.perform(patch("/star/{star-id}",star.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 평점수정_평점없음() throws Exception{
        //given
        StarUpdateRequest request = StarUpdateRequest.buildForTest(3);

        //when
        final ResultActions resultActions = mvc.perform(patch("/star/{star-id}",0L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Star with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 평점삭제() throws Exception{
        //given
        Star star = starSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(delete("/star/{star-id}",star.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());

    }

    @Test
    public void 평점삭제_평점없음() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(delete("/star/{star-id}",0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Star with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));

    }
}
