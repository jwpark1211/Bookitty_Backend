package capstone.bookitty.domain.member.application;

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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    public IdResponse saveMember(MemberSaveRequest request) {
        log.info("회원 저장 요청 - email: {}", request.email());
        Member member = Member.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .birthDate(request.birthdate())
                .gender(request.gender())
                .build();

        try {
            memberRepository.saveAndFlush(member);
            log.info("회원 저장 완료 - memberId: {}, email: {}", member.getId(), member.getEmail());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already in use.");
        }

        return IdResponse.of(member);
    }


    @Transactional(readOnly = true)
    public BoolResponse isEmailUnique(String email) {
        log.info("이메일 중복 검사 요청 - email: {}", email);
        boolean isUnique = !memberRepository.existsByEmail(email);
        log.info("이메일 중복 검사 결과 - email: {}, isUnique: {}", email, isUnique);
        return BoolResponse.of(isUnique);
    }

    @Transactional
    public TokenResponse login(MemberLoginRequest request) {
        log.debug("사용자 인증 진행 - email: {}", request.email());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        log.debug("인증 완료 - email: {}", request.email());

        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        log.debug("JWT 토큰 생성 완료 - email: {}, accessToken: {}, refreshToken: {}",
                request.email(), jwtToken.accessToken(), jwtToken.refreshToken());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("Member not found."));
        log.info("회원 정보 조회 완료 - memberId: {}, email: {}", member.getId(), request.email());

        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(jwtToken.refreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);
        log.debug("RefreshToken 저장 완료 - key: {}", authentication.getName());

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

        Member member = memberRepository.findByEmail(refreshToken.getKey())
                .orElseThrow(() -> new EntityNotFoundException("Member not found."));

        return TokenResponse.of(member.getId(), jwtToken, member.getProfileImg(), member.getName());
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfoWithId(Long memberId) {
        log.info("회원 정보 조회 요청 - memberId: {}", memberId);
        return memberRepository.findById(memberId)
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    @Transactional(readOnly = true)
    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        log.info("전체 회원 정보 조회 요청");
        return memberRepository.findAll(pageable)
                .map(MemberInfoResponse::from);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInfo(){
        log.info("로그인 한 회원의 정보 조회 요청");
        return memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new RuntimeException("No login user information."));
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

    @Transactional
    public void deleteMember(Long memberId) {
        log.info("회원 삭제 요청 - memberId: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        memberRepository.delete(member);
        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }
}
