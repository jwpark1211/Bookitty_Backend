package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.util.SecurityUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
class MemberQueryServiceTest {
    @Autowired MemberQueryService memberService;
    @Autowired MemberRepository memberRepository;

    @AfterEach
    void tearDown(){
        memberRepository.deleteAllInBatch();
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
            memberRepository.save(createRegularMember("김철수", "duplicate@naver.com"));

            //when
            BoolResponse boolResponse = memberService.isEmailUnique("duplicate@naver.com");

            //then
            assertThat(boolResponse.isUnique()).isFalse();
        }
    }

    @Nested
    @DisplayName("Id로 회원 정보 조회 메서드 Test Cases")
    class getMemberInfoWithIdTest{
        @Test
        @DisplayName("존재하는 회원의 Id로 요청 시 회원 정보를 반환한다.")
        void returnsMemberInfo_whenMemberExistsById(){
            //given
            Member member = createRegularMember("홍길동","1234abc@naver.com");
            memberRepository.save(member);

            //when
            MemberInfoResponse memberInfoResponse = memberService.getMemberInfoWithId(member.getId());

            //then
            assertThat(memberInfoResponse.email()).isEqualTo("1234abc@naver.com");
            assertThat(memberInfoResponse.name()).isEqualTo("홍길동");
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
            Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));
            Member member1 = createRegularMember("홍길동","user1@naver.com");
            Member member2 = createRegularMember("김영희","user2@naver.com");
            memberRepository.saveAll(List.of(member1, member2));

            // when
            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(pageable);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(
                            "email", "name")
                    .containsExactlyInAnyOrder(
                            tuple("user1@naver.com","홍길동"),
                            tuple("user2@naver.com","김영희"));
        }
        @Test
        @DisplayName("회원이 한 명도 존재하지 않는 경우 빈 페이지를 반환한다.")
        void returnsEmptyPage_whenNoMembersExist() {
            // given
            Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));

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
            Pageable pageable = PageRequest.of(1, 2, Sort.by("id"));
            Member member1 = createRegularMember("홍길동","user1@naver.com");
            Member member2 = createRegularMember("김영희","user2@naver.com");
            Member member3 = createRegularMember("유재석","user3@naver.com");
            Member member4 = createRegularMember("박명수","user4@naver.com");
            memberRepository.saveAll(List.of(member1, member2, member3, member4));

            //when
            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(pageable);

            //then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(
                            "email", "name")
                    .containsExactlyInAnyOrder(
                            tuple("user3@naver.com","유재석"),
                            tuple("user4@naver.com","박명수"));
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
            Member member = createRegularMember("홍길동", email);
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

    private Member createRegularMember(String name, String email){
        return Member.createUser(name, email,
                "encodedPassword",  Gender.MALE,
                LocalDate.of(1990, 1, 1));
    }
}