package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.api.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.application.memberApplication.MemberCommandService;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberCommandServiceTest {
    @Autowired
    MemberCommandService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberTestFixture memberFixture;

    @Nested
    @DisplayName("회원가입 요청 메서드 Test Cases")
    class SaveMemberTest {
        @Test
        @DisplayName("이메일이 중복되지 않고, 유효한 요청이 들어올 때 정상적으로 회원이 저장됩니다.")
        void successSaveMember() {
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
        void failDuplicateEmail() {
            //given
            memberRepository.save(memberFixture.createMember().email("duplicate@naver.com").build());
            MemberSaveRequest request = memberFixture.createMemberSaveRequest().email("duplicate@naver.com").build();

            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("email is already in use");
        }
    }

    @Nested
    @DisplayName("회원 삭제 메서드 deleteMember Test Cases")
    class DeleteMemberTest {

        @Test
        @DisplayName("본인이 본인을 삭제 요청하는 경우 성공적으로 삭제된다.")
        void successDeleteSelf() {
            // given
            String email = "email@gmail.com";
            Member member = memberRepository.save(
                    memberFixture.createMember().email(email).build());

            try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil(email)) {
                // when
                memberService.deleteMember(member.getId());

                // then
                boolean exists = memberRepository.existsById(member.getId());
                assertThat(exists).isFalse();
            }
        }

        @Test
        @DisplayName("Admin이 회원 삭제 요청 시 성공적으로 삭제된다.")
        void successDeleteAdmin() {
            // given
            String adminEmail = "admin@gmail.com";
            Member admin = memberRepository.save(memberFixture.createMember()
                    .email(adminEmail).authority(Authority.ROLE_ADMIN).build());
            Member member = memberRepository.save(memberFixture.createMember().build());

            try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil(adminEmail)) {
                // when
                memberService.deleteMember(member.getId());

                // then
                boolean exists = memberRepository.existsById(member.getId());
                assertThat(exists).isFalse();
            }
        }

        @Test
        @DisplayName("본인이 아닌 다른 사람을 삭제 요청 시 예외가 발생한다.")
        void failDeleteOtherMember() {
            // given
            Member member = memberRepository.save(
                    memberFixture.createMember().build());
            Member other = memberRepository.save(
                    memberFixture.createMember().email("other@gmail.com").build());

            try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil("other@gmail.com")) {
                // when + then
                assertThatThrownBy(() -> memberService.deleteMember(member.getId()))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Access denied");
            }
        }

        @Test
        @DisplayName("존재하지 않는 회원의 id로 삭제 요청 시 예외가 발생한다.")
        void failDeleteNonExistentMember() {
            // given
            Long nonExistentId = 999L; // Assuming this ID does not exist
            Member member = memberRepository.save(
                    memberFixture.createMember().email("logined@gmail.com").build());

            try (MockedStatic<SecurityUtil> mocked = mockSecurityUtil("logined@gmail.com")) {
                assertThatThrownBy(() -> memberService.deleteMember(nonExistentId))
                        .isInstanceOf(MemberNotFoundException.class)
                        .hasMessageContaining("not found");
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