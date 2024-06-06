package capstone.bookitty.domain.service;

import capstone.bookitty.domain.dto.TokenRequestDTO;
import capstone.bookitty.domain.dto.TokenResponseDTO;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.RefreshToken;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.RefreshTokenRepository;
import capstone.bookitty.jwt.JwtToken;
import capstone.bookitty.jwt.JwtTokenProvider;
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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import static capstone.bookitty.domain.dto.MemberDTO.*;

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
    private final S3Service s3Service;
    private final RedisUtil redisUtil;

    @Transactional
    public IdResponse saveMember(MemberSaveRequest request) {
        if(memberRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email already in use.");

        Member member = Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .birthDate(request.getBirthdate())
                .gender(request.getGender())
                .build();

        memberRepository.save(member);
        return new IdResponse(member.getId());
    }

    public BoolResponse isEmailUnique(String email) {
        return new BoolResponse(!memberRepository.existsByEmail(email));
    }

    @Transactional
    public TokenResponseDTO login(MemberLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new EntityNotFoundException("Member not found."));
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(jwtToken.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);
        return new TokenResponseDTO(member.getId(), jwtToken,member.getProfileImg(),member.getName());
    }

    @Transactional
    public TokenResponseDTO reissue(TokenRequestDTO tokenRequestDto) {
        if (!jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token is not valid.");
        }
        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User is already logged out."));
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("The user information in the refresh token does not match.");
        }
        JwtToken jwtToken = jwtTokenProvider.generateTokenDto(authentication);
        RefreshToken newRefreshToken = refreshToken.updateValue(jwtToken.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        log.info("refreshToken.getKey():"+refreshToken.getKey());
        Member member = memberRepository.findByEmail(refreshToken.getKey())
                .orElseThrow(()->new EntityNotFoundException("Member not found."));

        return new TokenResponseDTO(member.getId(),jwtToken,member.getProfileImg(),member.getName());
    }

    public MemberInfoResponse getMemberInfoWithId(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberInfoResponse::of)
                .orElseThrow(()-> new EntityNotFoundException("Member not found."));
    }

    public Page<MemberInfoResponse> getAllMemberInfo(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberInfoResponse::of);
    }

    public MemberInfoResponse getMyInfo(){
        log.info("service entry");
        log.info("SecurityUtil.getCurrentMemberEmail{}",SecurityUtil.getCurrentMemberEmail());
        return memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberInfoResponse::of)
                .orElseThrow(() -> new RuntimeException("No login user information."));
    }

    @Transactional
    public void logout(TokenRequestDTO tokenRequestDTO) {
        String refreshToken = tokenRequestDTO.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDTO.getAccessToken());
        String userEmail = authentication.getName();

        RefreshToken token = refreshTokenRepository.findByKey(userEmail)
                .orElseThrow(() -> new RuntimeException("User is already logged out or token is invalid."));

        refreshTokenRepository.delete(token);

        Long expiration = jwtTokenProvider.getExpiration(tokenRequestDTO.getAccessToken());
        redisUtil.setBlackList(tokenRequestDTO.getAccessToken(), "access_token", expiration);
    }

    @Transactional
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
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new EntityNotFoundException("member not found."));
        memberRepository.delete(member);
    }
}
