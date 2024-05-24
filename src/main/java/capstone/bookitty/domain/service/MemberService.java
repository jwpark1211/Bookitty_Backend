package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import static capstone.bookitty.domain.dto.MemberDTO.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @Transactional
    public IdResponse saveMember(MemberSaveRequest request) {
        if(memberRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email already in use.");

        Member member = Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(request.getPassword())
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
    public void login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new EntityNotFoundException("Member not found."));
        if(!member.getPassword().equals(request.getPassword()))
            throw new IllegalArgumentException("Invalid login credentials.");
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

    @Transactional
    public void updateProfile(Long memberId, MultipartFile profileImg)
            throws MultipartException, IOException {
        try {
            if (profileImg.isEmpty()) {
                throw new MultipartException("The file is not valid.");
            }
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("member not found."));
            String imageUrl = s3Service.uploadFile(profileImg);
            member.updateProfile(imageUrl);
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
