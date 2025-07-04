package capstone.bookitty.domain.member.application.MockTest;

import capstone.bookitty.domain.member.application.MemberService;
import capstone.bookitty.domain.member.domain.Authority;
import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.dto.IdResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Slf4j
@Disabled("목테스트 - 공부용")
class MockMemberServiceTest {

    @InjectMocks private MemberService memberService;
    @Mock private MemberRepository memberRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private static final String EMAIL = "test@example.com";
    private static final String NAME = "테스트";
    private static final Long MEMBER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    MemberSaveRequest request;
    Member member;


    @BeforeEach
    void setUp() {
        request = new MemberSaveRequest(EMAIL, "!Passwordw23", Gender.MALE,
                LocalDate.of(2000,1,1), NAME);

        member = Member.create(NAME, EMAIL, new Password("!Passwordw23"), "profile.jpg", Gender.MALE, LocalDate.of(2001,12,11),
                Authority.ROLE_USER, passwordEncoder);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SaveMemberTest {
        @Test
        @DisplayName("회원가입 성공")
        void 회원가입_성공() {
            given(memberRepository.existsByEmail(request.email())).willReturn(false);
            given(memberRepository.save(any(Member.class)))
                    .willAnswer(invocation -> {
                        Member m = invocation.getArgument(0);
                        //m.setId(MEMBER_ID);
                        return m;
                    });

            IdResponse response = memberService.saveMember(request);

            assertThat(response.id()).isEqualTo(MEMBER_ID);
        }

        @Test
        @DisplayName("이메일이 이미 존재하는 경우 예외 발생")
        void 이메일_중복() {
            given(memberRepository.existsByEmail(request.email())).willReturn(true);

            assertThatThrownBy(() -> memberService.saveMember(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("이미 사용 중인 이메일입니다");

            verify(memberRepository, never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("이메일 중복 확인 테스트")
    class IsEmailUnique {
        @Test
        @DisplayName("이메일이 존재하지 않으면 true 반환")
        void 이메일_존재하지_않음() {
            given(memberRepository.existsByEmail("nope@email.com")).willReturn(false);

            BoolResponse result = memberService.isEmailUnique("nope@email.com");

            assertThat(result.isUnique()).isTrue();
        }

        @Test
        @DisplayName("이메일이 존재하면 false 반환")
        void 이메일_존재함() {
            given(memberRepository.existsByEmail("exists@email.com")).willReturn(true);

            BoolResponse result = memberService.isEmailUnique("exists@email.com");

            assertThat(result.isUnique()).isFalse();
        }
    }

    @Nested
    @DisplayName("회원 ID로 정보 조회 테스트")
    class GetMemberInfoWithIdTest {
        @Test
        @DisplayName("회원이 존재하면 MemberInfoResponse 반환")
        void 회원존재_정상조회() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

            MemberInfoResponse result = memberService.getMemberInfoWithId(MEMBER_ID);

            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.email()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외 발생")
        void 회원없음_예외() {
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getMemberInfoWithId(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("전체 회원 조회 테스트")
    class GetAllMemberInfoTest {
        @Test
        @DisplayName("회원이 존재할 때 페이징 된 결과 반환")
        void 회원이_한명이상_페이징_조회_성공() {
            List<Member> memberList = List.of(member);
            Page<Member> page = new PageImpl<>(memberList);
            given(memberRepository.findAll(any(Pageable.class))).willReturn(page);

            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(PAGEABLE);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).email()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("회원이 존재하지 않을 때 빈 페이지 반환")
        void 회원_없음_빈페이지() {
            Page<Member> emptyPage = new PageImpl<>(List.of());
            given(memberRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

            Page<MemberInfoResponse> result = memberService.getAllMemberInfo(PAGEABLE);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("회원 삭제 테스트")
    class DeleteMemberTest {
        @Test
        @DisplayName("회원이 존재하면 삭제 성공")
        void 회원_삭제_성공() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

            memberService.deleteMember(MEMBER_ID);

            verify(memberRepository).delete(member);
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외 발생")
        void 회원_삭제_실패_존재하지_않음() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.deleteMember(MEMBER_ID))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }
}
