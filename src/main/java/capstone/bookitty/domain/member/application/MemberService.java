package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.exception.DuplicateEmailException;
import capstone.bookitty.domain.member.exception.RefreshTokenSaveException;
import capstone.bookitty.global.dto.BoolResponse;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.member.dto.MemberInfoResponse;
import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.dto.MemberSaveRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.global.authentication.RefreshToken;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.RefreshTokenRepository;
import capstone.bookitty.global.authentication.JwtToken;
import capstone.bookitty.global.authentication.JwtTokenProvider;
import capstone.bookitty.global.util.RedisUtil;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    //private final S3Service s3Service;
    private final RedisUtil redisUtil;

    @Transactional
    public IdResponse saveMember(MemberSaveRequest request) {
        log.info("회원 저장 요청 - email: {}", request.email());

        if (memberRepository.existsByEmail(request.email())) {
            log.warn("회원 저장 실패 - 중복 이메일: {}", request.email());
            throw new DuplicateEmailException(request.email());
        }

        Member member = Member.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .birthDate(request.birthdate())
                .gender(request.gender())
                .build();

        memberRepository.save(member);
        log.info("회원 저장 완료 - memberId: {}, email: {}", member.getId(), member.getEmail());

        return IdResponse.of(member);
    }

    public BoolResponse isEmailUnique(String email) {
        log.info("이메일 중복 검사 요청 - email: {}", email);
        boolean isUnique = !memberRepository.existsByEmail(email);
        log.info("이메일 중복 검사 결과 - email: {}, isUnique: {}", email, isUnique);
        return BoolResponse.of(isUnique);
    }

    @Transactional
    public TokenResponse login(MemberLoginRequest request) {
        log.debug("사용자 인증 진행 - email: {}", request.email());

        Authentication authentication = authenticateUser(request);
        JwtToken jwtToken = generateTokens(authentication);
        Member member = findMemberByEmail(request.email());
        saveRefreshToken(authentication, jwtToken);

        log.info("로그인 성공 - memberId: {}, email: {}", member.getId(), request.email());
        return new TokenResponse(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    @Transactional
    public TokenResponse reissue(TokenRequest tokenRequest) {
        log.info("토큰 재발급 요청");

        if (!jwtTokenProvider.validateToken(tokenRequest.refreshToken())) {
            log.warn("토큰 재발급 실패 - 유효하지 않은 Refresh Token");
            throw new RuntimeException("Refresh Token is not valid.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequest.accessToken());
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User is already logged out."));

        if (!refreshToken.getValue().equals(tokenRequest.refreshToken())) {
            log.warn("토큰 재발급 실패 - Refresh Token 불일치");
            throw new RuntimeException("The user information in the refresh token does not match.");
        }

        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        refreshToken.updateValue(jwtToken.refreshToken());
        refreshTokenRepository.save(refreshToken);

        log.info("토큰 재발급 성공 - userEmail: {}", refreshToken.getKey());
        Member member = findMemberByEmail(refreshToken.getKey());

        return TokenResponse.of(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    @Transactional
    public void logout(TokenRequest tokenRequest) {
        log.info("로그아웃 요청 - accessToken: {}", tokenRequest.accessToken());

        String refreshToken = tokenRequest.refreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("로그아웃 실패 - 유효하지 않은 Refresh Token");
            throw new RuntimeException("Invalid Refresh Token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequest.accessToken());
        String userEmail = authentication.getName();

        RefreshToken token = refreshTokenRepository.findByKey(userEmail)
                .orElseThrow(() -> new RuntimeException("User is already logged out or token is invalid."));

        refreshTokenRepository.delete(token);
        Long expiration = jwtTokenProvider.getExpiration(tokenRequest.accessToken());
        redisUtil.setBlackList(tokenRequest.accessToken(), "access_token", expiration);

        log.info("로그아웃 성공 - email: {}", userEmail);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        log.info("회원 삭제 요청 - memberId: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        memberRepository.delete(member);
        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }

    public MemberInfoResponse getMemberInfoWithId(Long memberId) {
        log.info("회원 정보 조회 요청 - memberId: {}", memberId);
        return memberRepository.findById(memberId)
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        log.info("전체 회원 정보 조회 요청");
        return memberRepository.findAll(pageable)
                .map(MemberInfoResponse::from);
    }

    public MemberInfoResponse getMyInfo(){
        log.info("로그인 한 회원의 정보 조회 요청");
        return memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new RuntimeException("No login user information."));
    }

    /*@Transactional
    public MemberInfoResponse updateProfile(Long memberId, MultipartFile profileImg)
            throws MultipartException, IOException {
        try {
            if (profileImg.isEmpty()) {
                throw new MultipartException("The file is not valid.");
            }
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("member not found."));
            String imageUrl = s3Service.uploadFile(profileImg);
            member.updateProfile(imageUrl);
            return new MemberInfoResponse(member.getId(),member.getEmail(),member.getProfileImg(),
                    member.getName(),member.getGender(),member.getBirthDate());
        } catch (MultipartException e) {
            throw e;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while updating the profile.", e);
        }
    }*/

    private Authentication authenticateUser(MemberLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }

    private JwtToken generateTokens(Authentication authentication) {
        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        log.debug("JWT 토큰 생성 완료 - accessToken: {}, refreshToken: {}",
                jwtToken.accessToken(), jwtToken.refreshToken());
        return jwtToken;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException());
    }

    private void saveRefreshToken(Authentication authentication, JwtToken jwtToken) {
        try {
            RefreshToken refreshToken = RefreshToken.builder()
                    .key(authentication.getName())
                    .value(jwtToken.refreshToken())
                    .build();
            refreshTokenRepository.save(refreshToken);
            log.debug("RefreshToken 저장 완료 - key: {}", authentication.getName());
        } catch (Exception e) {
            log.error("RefreshToken 저장 실패 - key: {}", authentication.getName(), e);
            throw new RefreshTokenSaveException("리프레시 토큰 저장 중 오류가 발생했습니다.");
        }
    }
}
