package capstone.bookitty.domain.service;

import capstone.bookitty.domain.dto.commonDto.BoolResponse;
import capstone.bookitty.domain.dto.commonDto.IdResponse;
import capstone.bookitty.domain.dto.memberDto.MemberInfoResponse;
import capstone.bookitty.domain.dto.memberDto.MemberLoginRequest;
import capstone.bookitty.domain.dto.memberDto.MemberSaveRequest;
import capstone.bookitty.domain.dto.tokenDto.TokenRequest;
import capstone.bookitty.domain.dto.tokenDto.TokenResponse;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.RefreshToken;
import capstone.bookitty.domain.exception.MemberNotFoundException;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.RefreshTokenRepository;
import capstone.bookitty.authentication.JwtToken;
import capstone.bookitty.authentication.JwtTokenProvider;
import capstone.bookitty.util.RedisUtil;
import capstone.bookitty.util.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
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
@Transactional(readOnly = true)
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
        if(memberRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email already in use.");

        Member member = Member.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .birthDate(request.birthdate())
                .gender(request.gender())
                .build();

        memberRepository.save(member);
        return IdResponse.of(member);
    }

    public BoolResponse isEmailUnique(String email) {
        return BoolResponse.of(!memberRepository.existsByEmail(email));
    }

    @Transactional
    public TokenResponse login(MemberLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(()-> new EntityNotFoundException("Member not found."));
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(jwtToken.refreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);
        return new TokenResponse(member.getId(), jwtToken,member.getProfileImg(),member.getName());
    }

    @Transactional
    public TokenResponse reissue(TokenRequest tokenRequest) {
        if (!jwtTokenProvider.validateToken(tokenRequest.refreshToken())) {
            throw new RuntimeException("Refresh Token is not valid.");
        }
        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequest.accessToken());
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User is already logged out."));
        if (!refreshToken.getValue().equals(tokenRequest.refreshToken())) {
            throw new RuntimeException("The user information in the refresh token does not match.");
        }
        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        RefreshToken newRefreshToken = refreshToken.updateValue(jwtToken.refreshToken());
        refreshTokenRepository.save(newRefreshToken);

        log.info("refreshToken.getKey():"+refreshToken.getKey());
        Member member = memberRepository.findByEmail(refreshToken.getKey())
                .orElseThrow(()->new EntityNotFoundException("Member not found."));

        return TokenResponse.of(member.getId(),jwtToken,member.getProfileImg(),member.getName());
    }

    public MemberInfoResponse getMemberInfoWithId(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberInfoResponse::from)
                .orElseThrow(()-> new MemberNotFoundException(memberId));
    }

    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberInfoResponse::from);
    }

    public MemberInfoResponse getMyInfo(){
        log.info("service entry");
        log.info("SecurityUtil.getCurrentMemberEmail{}",SecurityUtil.getCurrentMemberEmail());
        return memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberInfoResponse::from)
                .orElseThrow(() -> new RuntimeException("No login user information."));
    }

    @Transactional
    public void logout(TokenRequest tokenRequest) {
        String refreshToken = tokenRequest.refreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequest.accessToken());
        String userEmail = authentication.getName();

        RefreshToken token = refreshTokenRepository.findByKey(userEmail)
                .orElseThrow(() -> new RuntimeException("User is already logged out or token is invalid."));

        refreshTokenRepository.delete(token);

        Long expiration = jwtTokenProvider.getExpiration(tokenRequest.accessToken());
        redisUtil.setBlackList(tokenRequest.accessToken(), "access_token", expiration);
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new MemberNotFoundException(memberId));
        memberRepository.delete(member);
    }
}
