package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import capstone.bookitty.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MemberApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberTestFixture memberFixture;

    @Nested
    @WithMockUser
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
                    .andDo(MockMvcResultHandlers.print())
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
    @DisplayName("이메일 중복 확인 API Test Cases")
    class isEmailUniqueTest {
        @Test
        @DisplayName("이메일이 중복되지 않은 경우 200과 true가 반환된다.")
        void success_whenEmailNotDuplicated_thenReturns200() throws Exception {
            //given

            //when + then
            mockMvc.perform(get("/members/email/unique")
                            .param("email", "1234abc@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isUnique").value(true));
        }

        @Test
        @DisplayName("이메일이 중복된 경우 200과 false가 반환된다.")
        void success_whenEmailDuplicated_thenReturns200() throws Exception {
            //given
            String email = "1234abc@naver.com";
            memberRepository.save(memberFixture.createMember().email(email).build());

            //when + then
            mockMvc.perform(get("/members/email/unique")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isUnique").value(false));
        }

        @Test
        @DisplayName("올바른 이메일 형식이 아닌 경우 400과 에러메시지가 반환된다.")
        void fail_whenInvalidEmailFormat_thenReturns400() throws Exception {
            // given
            String invalidEmail = "invalid-email-format";

            // when + then
            mockMvc.perform(get("/members/email/unique")
                            .param("email", invalidEmail))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Input Value"));

        }

        @Test
        @DisplayName("이메일이 비어 있는 경우 400과 에러 메시지가 반환된다.")
        void fail_whenEmailIsNull_thenReturns400() throws Exception {
            // given
            String nullEmail = "";

            // when + then
            mockMvc.perform(get("/members/email/unique")
                            .param("email", nullEmail))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Input Value"));
        }

        @Test
        @DisplayName("특수문자 포함된 유효한 이메일이 주어지면 200과 true가 반환된다.")
        void suceess_whenEmailContainsSpecial_thenReturns200() throws Exception {
            //given
            String specialEmail = "12343%ab@gmail.com";

            //when + then
            mockMvc.perform(get("/members/email/unique")
                            .param("email", specialEmail))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isUnique").value(true));
        }

        @Test
        @DisplayName("이메일이 최대 길이를 초과하는 경우 400과 에러 메시지가 반환된다.")
        void fail_whenEmailExceedsMaxLength_thenReturns400() throws Exception {
            // given
            String longEmail = "a".repeat(257) + "@example.com"; // 257자 이메일

            // when + then
            mockMvc.perform(get("/members/email/unique")
                            .param("email", longEmail))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Input Value"));
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("id로 회원 조회 API Test Cases")
    class findOneMemberTest {
        @Test
        @DisplayName("유효한 회원 ID로 조회 시 200과 회원 정보가 반환된다.")
        void success_whenValidMemberId_thenReturns200() throws Exception {
            //given
            memberRepository.save(memberFixture.createMember().build());

            //when + then
            mockMvc.perform(get("/members/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 조회 시 404가 반환된다.")
        void fail_whenNonExistentMemberId_thenReturns404() throws Exception {
            //given
            Long nonExistentId = 999L; // 존재하지 않는 ID

            //when + then
            mockMvc.perform(get("/members/" + nonExistentId)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Member ID[999] is not found"));
        }

        @Test
        @DisplayName("잘못된 형식의 회원 ID로 조회 시 404가 반환된다.")
        void fail_whenInvalidMemberId_thenReturns404() throws Exception {
            //given
            Long invalidId = 0L;

            //when + then
            mockMvc.perform(get("/members/" + invalidId)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Member ID[0] is not found"));
        }
    }
}