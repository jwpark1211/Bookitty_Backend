package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberCommandServiceTest {
    @Autowired MemberCommandService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberTestFixture memberFixture;

    @Nested
    @DisplayName("회원가입 요청 메서드 Test Cases")
    class SaveMemberTest{
        @Test
        @DisplayName("이메일이 중복되지 않고, 유효한 요청이 들어올 때 정상적으로 회원이 저장됩니다.")
        void successSaveMember(){
            //given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest()
                    .name("홍길동").email("1234abc@naver.com").build();

            //when
            Long id = memberService.saveMember(request);

            //then
            Member member = memberRepository.findById(id).orElseThrow();
            assertThat(member.getEmail()).isEqualTo("1234abc@naver.com");
            assertThat(member.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 요청 시 예외가 발생합니다.")
        void failDuplicateEmail(){
            //given
            memberRepository.save(memberFixture.createMember().email("duplicate@naver.com").build());
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().email("duplicate@naver.com").build();

            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("email is already in use");
        }

        @Test
        @DisplayName("비밀번호가 생성 규칙(길이)에 맞지 않는 경우 예외가 발생합니다.")
        // 비밀번호는 8~20자 사이여야 합니다.
        void failUnValidPassword(){
            //given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().password("Wlqo13").build();

            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Passwords must contain at least one English case, number, and special characters, and must be 8 or more and 20 or less in length");
        }

        @Test
        @DisplayName("비밀번호가 비어있는 경우 예외가 발생합니다.")
        void failNullPassword(){
            //given
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().password(null).build();

            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password cannot be null or blank");
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