package capstone.bookitty.domain.member.dto;

import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MemberInfoResponse(
        Long id,
        String email,
        String profileImg,
        String name,
        Gender gender,
        @DateTimeFormat(pattern="yyyy-MM-dd")
        LocalDate birthDate
) {
    public static MemberInfoResponse of(Long id, String email, String profileImg, String name,
                                        Gender gender, LocalDate birthDate){
        return new MemberInfoResponse(id, email, profileImg, name, gender, birthDate);
    }

    public static MemberInfoResponse from(Member member){
        return new MemberInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getProfileImg(),
                member.getName(),
                member.getGender(),
                member.getBirthDate()
        );
    }
}
