package capstone.bookitty.controllerTest;

import capstone.bookitty.IntergrationTest;
import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.State;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static capstone.bookitty.domain.dto.BookStateDTO.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookStateControllerTest extends IntergrationTest {

    @Test
    public void 아이디로개별조회() throws Exception{
        //given
        BookState state = stateSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(get("/state/{state-id}",state.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(state.getId()))
                .andExpect(jsonPath("$.data.memberId").value(state.getMember().getId()))
                .andExpect(jsonPath("$.data.isbn").value(state.getIsbn()))
                .andExpect(jsonPath("$.data.state").value(state.getState().toString()))
                .andExpect(jsonPath("$.data.categoryName").value(state.getCategoryName()))
                .andExpect(jsonPath("$.data.bookTitle").value(state.getBookTitle()))
                .andExpect(jsonPath("$.data.bookAuthor").value(state.getBookAuthor()))
                .andExpect(jsonPath("$.data.bookImgUrl").value(state.getBookImgUrl()));
    }

    @Test
    public void 아이디로개별조회_아이디없음() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(get("/state/{state-id}",0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("BookState with ID 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 책상태생성() throws Exception{
       //given
       final Member member = memberSetup.save();
       final StateSaveRequest request = StateSaveRequest.buildForTest("testIsbn",member.getId(), State.READ_ALREADY);

       //when
       final ResultActions resultActions = mvc.perform(post("/state/new")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsBytes(request)))
               .andDo(print());

       //then
       resultActions.andExpect(status().isOk());
    }

    @Test
    public void 책상태생성_valid오류_회원없음() throws Exception{
        //given
        final StateSaveRequest request = StateSaveRequest.buildForTest("testIsbn",0L, State.READ_ALREADY);

        //when
        final ResultActions resultActions = mvc.perform(post("/state/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Member not found for ID: 0"))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 전체상태정보() throws Exception{
        //given
        List<BookState> states = stateSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/state/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void isbn으로상태정보가져오기() throws Exception{
        //given
        List<BookState> states = stateSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/state/isbn/{isbn}",states.get(1).getIsbn())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    public void memberId로상태정보가져오기() throws Exception{
        //given
        List<BookState> states = stateSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/state/member/{member-id}",states.get(1).getMember().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    public void memberId로책정보가져오기_회원없음() throws Exception{
        //given
        List<BookState> states = stateSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/state/member/{member-id}",0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Member with ID: 0 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 상태삭제() throws Exception{
        //given
        BookState state = stateSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(delete("/state/{state-id}",state.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 상태삭제_상태정보없음() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(delete("/state/{state-id}",0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("BookState not found for ID: 0"))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    // 달별책개수,카테고리별책개수
}
