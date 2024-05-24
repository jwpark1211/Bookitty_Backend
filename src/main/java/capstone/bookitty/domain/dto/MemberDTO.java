package capstone.bookitty.domain.dto;

import capstone.bookitty.domain.annotation.ValidEnum;
import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class MemberDTO {
    @Data
    @AllArgsConstructor
    public static class MemberSaveRequest {
        @NotBlank(message = "Email is a required entry value.")
        @Email(message = "Email format is not valid.")
        private String email;
        @NotBlank(message = "Password is a required entry value.")
        @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
                message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
        private String password;
        @ValidEnum(enumClass = Gender.class, message = "Invalid gender")
        private Gender gender; //FEMALE 혹은 MALE
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthdate;
        @NotBlank(message = "name is a required entry value.")
        private String name;
    }

    @Data
    public static class MemberLoginRequest {
        @NotBlank(message = "Email is a required entry value.")
        @Email(message = "Email format is not valid.")
        private String email;
        @NotBlank(message = "Password is a required entry value.")
        private String password;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IdResponse{
        private Long id;
        public static IdResponse of (Member member){
            return new IdResponse((member.getId()));
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoolResponse{
        private boolean unique;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberInfoResponse{
        private Long id;
        private String email;
        private String profileImg;
        private String name;
        private Gender gender;
        @DateTimeFormat(pattern="yyyy-MM-dd")
        private LocalDate birthDate;

        public static MemberInfoResponse of(Member member){
            return new MemberInfoResponse(member.getId(), member.getEmail(), member.getProfileImg(),
                    member.getName(), member.getGender(), member.getBirthDate());
        }
    }
}
