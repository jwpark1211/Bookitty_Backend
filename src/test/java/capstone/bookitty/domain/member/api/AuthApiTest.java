package capstone.bookitty.domain.member.api;

import capstone.bookitty.domain.member.api.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.domain.Member;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberTestFixture memberFixture;
    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("로그인 API Test Cases")
    class loginTest {

        @Test
        @DisplayName("올바른 로그인 요청 시 200이 반환된다.")
            //FIXME : 테스트 메서드 이름은 success_login 정도가 좋을까? 혹은 success_whenValidLoginRequest_thenReturns200이 좋을까
        void success_whenValidLoginRequest_thenReturns200() throws Exception {
            // given
            Member member = memberFixture.createMemberWithRawPassword("Valid123!")
                    .email("email@gmail.com").build();
            memberRepository.save(member);
            MemberLoginRequest request = memberFixture.createMemberLoginRequest()
                    .email("email@gmail.com")
                    .password("Valid123!")
                    .build();

            //when + then
            mockMvc.perform(post("/members/login")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idx").value(member.getId()));
        }

        @Test
        @DisplayName("로그인 요청 시 이메일이 누락되면 400이 반환된다.")
        void fail_whenEmailMissing_thenReturns400() throws Exception {
            // given
            MemberLoginRequest request = memberFixture.createMemberLoginRequest()
                    .email("").build();

            // when + then
            mockMvc.perform(post("/members/login")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("로그인 요청 시 비밀번호가 누락되면 400이 반환된다.")
        void fail_whenPasswordMissing_thenReturns400() throws Exception {
            // given
            MemberLoginRequest request = memberFixture.createMemberLoginRequest()
                    .password("").build();

            // when + then
            mockMvc.perform(post("/members/login")
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

}