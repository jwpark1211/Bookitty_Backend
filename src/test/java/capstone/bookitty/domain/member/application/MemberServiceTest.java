package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.global.util.SecurityUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown(){
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("회원가입 요청 메서드 Test Cases")
    class SaveMemberTest{
        @Test
        @DisplayName("이메일이 중복되지 않고, 유효한 요청이 들어올 때 정상적으로 회원이 저장됩니다.")
        void successSaveMember(){
            //given
            MemberSaveRequest request = new MemberSaveRequest
                    ("1234abc@naver.com","Wlqo134@",Gender.FEMALE,
                            LocalDate.of(1999,1,1),"홍길동");

            //when
            IdResponse idResponse = memberService.saveMember(request);

            //then
            assertThat(idResponse.id()).isNotNull();
            Member member = memberRepository.findById(idResponse.id()).orElseThrow();
            assertThat(member.getEmail()).isEqualTo("1234abc@naver.com");
            assertThat(member.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 요청 시 예외가 발생합니다.")
        void failDuplicateEmail(){
            //given
            memberRepository.save(Member.create("홍길동","duplicate@naver.com",
                    new Password("Wlqo134@"),null,Gender.FEMALE,
                    LocalDate.of(1999,1,1), passwordEncoder));
            MemberSaveRequest request = new MemberSaveRequest
                    ("duplicate@naver.com","Wlqo134@",Gender.FEMALE,
                            LocalDate.of(1999,1,1),"홍길동");

            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("이미 사용 중인 이메일입니다");
        }

        @Test
        @DisplayName("비밀번호가 생성 규칙(길이)에 맞지 않는 경우 예외가 발생합니다.")
        // 비밀번호는 8~20자 사이여야 합니다.
        void failUnValidPassword(){
            //given
            MemberSaveRequest request = new MemberSaveRequest
                    ("1234abc@naver.com","Wlqo13",Gender.FEMALE,
                            LocalDate.of(1999,1,1),"홍길동");
            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("길이는 8자 이상 20자 이하여야 합니다.");
        }

        @Test
        @DisplayName("비밀번호가 비어있는 경우 예외가 발생합니다.")
        void failNullPassword(){
            //given
            MemberSaveRequest request = new MemberSaveRequest
                    ("1234abc@naver.com",null,Gender.FEMALE,
                            LocalDate.of(1999,1,1),"홍길동");
            //when + then
            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호는 비어있을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("이메일 중복 확인 메서드 Test Cases")
    class isEmailUniqueTest{
        @Test
        @DisplayName("이메일이 중복되지 않는 경우 true를 반환합니다.")
        void returnsTrueWhenEmailIsNotDuplicated(){
            //given

            //when
            BoolResponse boolResponse = memberService.isEmailUnique("unDuplicate@naver.com");

            //then
            assertThat(boolResponse.isUnique()).isTrue();

        }

        @Test
        @DisplayName("이메일이 중복되는 경우 false를 반환합니다.")
        void returnsFalseWhenEmailIsDuplicated(){
            //given
            memberRepository.save(Member.create("홍길동","duplicate@naver.com",
                    new Password("Wlqo134@"),null,Gender.FEMALE,
                    LocalDate.of(1999,1,1), passwordEncoder));

            //when
            BoolResponse boolResponse = memberService.isEmailUnique("duplicate@naver.com");

            //then
            assertThat(boolResponse.isUnique()).isFalse();
        }
    }

    @Nested
    @DisplayName("회원 정보 삭제 메서드 Test Cases")
    class deleteMemberTest {
        @Test
        @DisplayName("일반 사용자가 다른 회원을 삭제하면 예외가 발생한다.")
        void userCannotDeleteAnotherUser() {
            // given
            Member member1 = Member.create("홍길동", "1234abc@naver.com",
                    new Password("Wlqo134@"), null, Gender.FEMALE,
                    LocalDate.of(1999, 1, 1), passwordEncoder);
            Member member2 = Member.create("김철수", "5678abc@naver.com",
                    new Password("Wlqo134@"), null, Gender.MALE,
                    LocalDate.of(2005, 3, 1), passwordEncoder);
            memberRepository.save(member1);
            memberRepository.save(member2);

            try (MockedStatic<SecurityUtil> ignored = mockSecurityUtil(member1.getEmail())) {
                // when + then
                assertThatThrownBy(() -> memberService.deleteMember(member2.getId()))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("삭제 권한이 없습니다.");
            }
        }
        @Test
        @DisplayName("관리자 권한이 있는 경우, 다른 회원을 삭제할 수 있다.")
        void adminCanDeleteOtherUser() {
            // given
            Member admin = Member.createAdmin("홍길동", "1234abc@naver.com",
                    new Password("Wlqo134@"), null, Gender.FEMALE,
                    LocalDate.of(1999, 1, 1), passwordEncoder);
            Member target = Member.create("김철수", "5678abc@naver.com",
                    new Password("Wlqo134@"), null, Gender.MALE,
                    LocalDate.of(2005, 3, 1), passwordEncoder);
            memberRepository.save(admin);
            memberRepository.save(target);

            try (MockedStatic<SecurityUtil> ignored = mockSecurityUtil(admin.getEmail())) {
                // when
                memberService.deleteMember(target.getId());

                // then
                Optional<Member> deleted = memberRepository.findById(target.getId());
                assertThat(deleted).isEmpty();
            }
        }
        @Test
        @DisplayName("자신의 회원 정보가 존재하는 경우 삭제할 수 있다.")
        void userCanDeleteOwnAccount() {
            // given
            Member member = Member.create("홍길동", "1234abc@naver.com",
                    new Password("Wlqo134@"), null, Gender.FEMALE,
                    LocalDate.of(1999, 1, 1), passwordEncoder);
            memberRepository.save(member);

            try (MockedStatic<SecurityUtil> ignored = mockSecurityUtil(member.getEmail())) {
                // when
                memberService.deleteMember(member.getId());

                // then
                Optional<Member> deleted = memberRepository.findById(member.getId());
                assertThat(deleted).isEmpty();
            }
        }
        @Test
        @DisplayName("자신의 회원 정보가 존재하지 않는 경우 예외가 발생한다.")
        void deleteFailsWhenUserNotFound(){
            //when + then
            assertThatThrownBy(() -> memberService.deleteMember(12L))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Id로 회원 정보 조회 메서드 Test Cases")
    class getMemberInfoWithIdTest{
        @Test
        @DisplayName("존재하는 회원의 Id로 요청 시 회원 정보를 반환한다.")
        void returnsMemberInfo_whenMemberExistsById(){
            //given
            Member member = Member.create("홍길동","1234abc@naver.com",
                    new Password("Wlqo134@"),null,Gender.FEMALE,
                    LocalDate.of(1999,1,1), passwordEncoder);
            memberRepository.save(member);

            //when
            MemberInfoResponse memberInfoResponse = memberService.getMemberInfoWithId(member.getId());

            //then
            assertThat(memberInfoResponse.email()).isEqualTo("1234abc@naver.com");
            assertThat(memberInfoResponse.name()).isEqualTo("홍길동");
            assertThat(memberInfoResponse.gender()).isEqualTo(Gender.FEMALE);
        }
        @Test
        @DisplayName("존재하지 않는 회원의 Id로 요청 시 예외가 발생한다.")
        void returnsException_whenMemberNotExistsById(){
            //given

            //when + then
            assertThatThrownBy(() -> memberService.getMemberInfoWithId(100L))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("모든 회원 정보 조회 메서드 Test Cases")
    class getAllMemberInfoTest {
        @Test
        @DisplayName("회원이 여러 명 존재하는 경우 회원 정보를 반환한다.")
        void returnsPagedMemberInfo_whenMultipleMembersExist() {
            // given
            Pageable pageable = PageRequest.of(0, 2);
            Member member1 = Member.create("홍길동", "user1@naver.com",
                    new Password("Password1@@!"), null, Gender.MALE,
                    LocalDate.of(1990, 1, 1), passwordEncoder);
            Member member2 = Member.create("김영희", "user2@naver.com",
                    new Password("Password2@@!"), null, Gender.FEMALE,
                    LocalDate.of(1992, 2, 2), passwordEncoder);
            memberRepository.save(member1);
            memberRepository.save(member2);

            // when
            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(pageable);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting("email")
                    .containsExactlyInAnyOrder("user1@naver.com", "user2@naver.com");
        }
        @Test
        @DisplayName("회원이 한 명도 존재하지 않는 경우 빈 페이지를 반환한다.")
        void returnsEmptyPage_whenNoMembersExist() {
            // given
            Pageable pageable = PageRequest.of(0, 2);

            // when
            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(pageable);

            // then
            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
        @Test
        @DisplayName("두 번째 페이지를 요청하면 해당 페이지에 해당하는 회원들이 조회된다.")
        void returnsCorrectMembers_whenSecondPageIsRequested(){
            //given
            Pageable pageable = PageRequest.of(1, 2);
            Member member1 = Member.create("홍길동", "user1@naver.com",
                    new Password("Password1@@!"), null, Gender.MALE,
                    LocalDate.of(1990, 1, 1), passwordEncoder);
            Member member2 = Member.create("김영희", "user2@naver.com",
                    new Password("Password2@@!"), null, Gender.FEMALE,
                    LocalDate.of(1992, 2, 2), passwordEncoder);
            Member member3 = Member.create("홍길동", "user3@naver.com",
                    new Password("Password1@@!"), null, Gender.MALE,
                    LocalDate.of(1990, 1, 1), passwordEncoder);
            Member member4 = Member.create("김영희", "user4@naver.com",
                    new Password("Password2@@!"), null, Gender.FEMALE,
                    LocalDate.of(1992, 2, 2), passwordEncoder);
            memberRepository.save(member1);
            memberRepository.save(member2);
            memberRepository.save(member3);
            memberRepository.save(member4);

            //when
            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(pageable);

            //then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting("email")
                    .containsExactlyInAnyOrder("user3@naver.com", "user4@naver.com");
        }
    }

    @Nested
    @DisplayName("로그인 된 회원 정보 조회 메서드 Test Cases")
    class getMyInfo {
        @Test
        @DisplayName("로그인된 회원의 정보가 존재하는 경우 회원 정보를 반환한다.")
        void returnsMemberInfo_whenAuthenticatedUserExists() {
            // given
            String email = "user@naver.com";
            Member member = Member.create("홍길동", email,
                    new Password("Wlqo134@"), null, Gender.MALE,
                    LocalDate.of(1990, 1, 1), passwordEncoder);
            memberRepository.save(member);

            try(MockedStatic<SecurityUtil> mocked = mockSecurityUtil(email)) {
                // when
                MemberInfoResponse result = memberService.getMyInfo();

                // then
                assertThat(result.email()).isEqualTo(email);
                assertThat(result.name()).isEqualTo("홍길동");
            }
        }
        @Test
        @DisplayName("로그인된 회원의 정보가 Security Util에 존재하지 않는 경우 예외가 발생한다.")
        void throwsException_whenCurrentUserNotAuthenticated() {
            try (MockedStatic<SecurityUtil> ignored = mockSecurityUtilToThrow()) {
                assertThatThrownBy(() -> memberService.getMyInfo())
                        .isInstanceOf(UnauthenticatedMemberException.class);
            }
        }

        @Test
        @DisplayName("로그인된 회원의 정보는 존재하지만 DB에 회원 정보가 없는 경우 예외가 발생한다.")
        void throwsException_whenEmailExistsButMemberNotFound() {
            String email = "ghost@naver.com";

            try (MockedStatic<SecurityUtil> ignored = mockSecurityUtil(email)) {
                assertThatThrownBy(() -> memberService.getMyInfo())
                        .isInstanceOf(MemberNotFoundException.class);
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