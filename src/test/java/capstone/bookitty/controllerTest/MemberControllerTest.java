package capstone.bookitty.controllerTest;

import static capstone.bookitty.domain.dto.MemberDTO.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import capstone.bookitty.IntergrationTest;
import capstone.bookitty.domain.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

public class MemberControllerTest extends IntergrationTest {

    @Test
    public void 회원_개별조회() throws Exception{
        //given
        Member member = memberSetup.save();

        //when
        final ResultActions resultActions  =  mvc.perform(get("/members/{id}", member.getId())
                                                .contentType(MediaType.APPLICATION_JSON))
                                                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.profileImg").value(member.getProfileImg()))
                .andExpect(jsonPath("$.data.gender").value(member.getGender().toString()))
                .andExpect(jsonPath("$.data.name").value(member.getName()))
                .andExpect(jsonPath("$.data.birthDate").value(member.getBirthDate().toString()));

    }

    @Test
    public void 회원_개별조회_회원이없는경우() throws Exception{
        //given

        //when
        final ResultActions resultActions  =  mvc.perform(get("/members/{id}", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("Member not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    public void 회원가입_성공() throws Exception{
        //given
        final MemberSaveRequest request = MemberSaveRequest
                .buildForTest("test@gmail.com","passWD!42","testName");

        //when
        final ResultActions resultActions = mvc.perform(post("/members/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void 회원가입_실패_valid오류() throws Exception{
        //given
        final MemberSaveRequest request = MemberSaveRequest
                .buildForTest("test@gmail.com","password","testName");

        //when
        final ResultActions resultActions = mvc.perform(post("/members/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("500"));
    }

    @Test
    public void 이메일중복확인_중복이아닌경우() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(
                get("/members/email/{email}/unique","neverUsed@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unique").value(true));
    }

    @Test
    public void 이메일중복확인_중복인경우() throws Exception{
        //given
        Member member = memberSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(
                        get("/members/email/{email}/unique",member.getEmail())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unique").value(false));
    }

    @Test
    public void 로그인() throws Exception{
        //given
        Member member = memberSetup.save();
        MemberLoginRequest request = MemberLoginRequest.buildForTest(
                member.getEmail(),memberSetup.getDefaultPassword());

        //when
        final ResultActions resultActions = mvc.perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.idx").value(member.getId()))
                .andExpect(jsonPath("$.data.jwtToken.grantType").value("Bearer"));
    }

    @Test
    public void 로그인_실패() throws Exception{
        //given
        MemberLoginRequest request = MemberLoginRequest.buildForTest(
                "fault@email.com","faultPw!2");

        //when
        final ResultActions resultActions = mvc.perform(post("/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMsg").value("Bad credentials"))
                .andExpect(jsonPath("$.errorCode").value("500"));
    }

    @Test
    public void 전체회원조회() throws Exception {
        //given
        List<Member> members = memberSetup.save(5);

        //when
        final ResultActions resultActions = mvc.perform(get("/members/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    public void 회원탈퇴() throws Exception{
        //given
        Member member = memberSetup.save();

        //when
        final ResultActions resultActions = mvc.perform(delete("/members/{id}",member.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk());

    }

    @Test
    public void 회원탈퇴_회원정보없음() throws Exception{
        //given

        //when
        final ResultActions resultActions = mvc.perform(delete("/members/{id}",0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMsg").value("member not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

}
