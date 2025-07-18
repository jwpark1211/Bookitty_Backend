package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mockStatic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class MemberCommandApiTest {

    // FIXME: SecurityUtil은 API와 Service 레이어 모두 Mock 처리 해야 하는가? (회원 탈퇴 관련 권한 확인 때문에...)
    // FIXME : 테스트 대상 객체는 테스트 내부에서 "sut"라고 지칭하는 게 좋을까?

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberTestFixture memberFixture;
    @Autowired
    private MemberRepository memberRepository;


    @Nested
    @DisplayName("회원가입 API Test Cases")
    class saveMemberTest {
        @Test
        @DisplayName("올바른 회원가입 요청 시 201이 반환되고 회원 ID가 포함된 응답이 반환된다.")
        void success_whenValidRequest_thenReturns201WithId() throws Exception {
            // given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().build();

            // when + then
            mockMvc.perform(post("/members/new")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("이메일이 누락된 경우 400과 에러 메시지가 반환된다.")
        void fail_whenEmailMissing_thenReturns400() throws Exception {
            // given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().email("").build();

            // when + then
            mockMvc.perform(post("/members/new")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("email"))
                    .andExpect(jsonPath("$.errors[0].reason").value("Email is a required entry value"));
        }

        @Test
        @DisplayName("이메일 형식이 잘못된 경우 400과 에러 메시지가 반환된다.")
        void fail_whenInvalidEmailFormat_thenReturns400() throws Exception {
            //given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().email("1234abc").build();

            //when + then
            mockMvc.perform(post("/members/new")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("email"))
                    .andExpect(jsonPath("$.errors[0].reason").value("Email format is not valid"));
        }

        @Test
        @DisplayName("비밀번호가 누락된 경우 400과 에러 메시지가 반환된다.")
        void fail_whenPasswordIsBlank_thenReturns400() throws Exception {
            // given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().password("").build();

            // when + then
            mockMvc.perform(post("/members/new")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("password"))
                    .andExpect(jsonPath("$.errors[0].reason").value("Password is a required entry value"));
        }

        @Test
        @DisplayName("이름이 비어있는 경우 400과 에러 메시지가 반환된다.")
        void fail_whenNameIsBlank_thenReturns400() throws Exception {
            //given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().name("").build();

            //when + then
            mockMvc.perform(post("/members/new")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].reason").value("name is a required entry value"));
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("회원탈퇴 API Test Cases")
    class deleteMemberTest {
        @Test
        @DisplayName("올바른 회원 탈퇴 요청 시 204가 반환된다.")
        void success_whenValidRequest_thenReturns204() throws Exception {
            // given
            Member member = memberRepository.save(
                    memberFixture.createMember().email("login@gmail.com").build());

            // when + then
            try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil(member.getEmail())) {
                mockMvc.perform(delete("/members/" + member.getId())
                                .with(csrf()))
                        .andExpect(status().isNoContent());
            }
        }

        @Test
        @DisplayName("잘못된 회원 id로 회원 탈퇴 요청 시 404가 반환된다.")
        void fail_whenInvalidMemberId_thenReturns404() throws Exception {
            // given
            Long invalidMemberId = 999L; // 존재하지 않는 회원 ID
            Member member = memberRepository.save(
                    memberFixture.createMember().email("login@gmail.com").build());

            // when + then
            try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil(member.getEmail())) {
                mockMvc.perform(delete("/members/" + 999L)
                                .with(csrf()))
                        .andExpect(status().isNotFound());
            }
        }
    }

    //== private Method ==//
    // SecurityUtil에서 정상적인 이메일 반환을 Mock
    private MockedStatic<SecurityUtil> mockSecurityUtil(String email) {
        MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class);
        mocked.when(SecurityUtil::getCurrentMemberEmail).thenReturn(email);
        return mocked;
    }

    // SecurityUtil에서 예외를 던지도록 Mock
    private MockedStatic<SecurityUtil> mockSecurityUtilToThrow() {
        MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class);
        mocked.when(SecurityUtil::getCurrentMemberEmail)
                .thenThrow(new UnauthenticatedMemberException());
        return mocked;
    }
}
