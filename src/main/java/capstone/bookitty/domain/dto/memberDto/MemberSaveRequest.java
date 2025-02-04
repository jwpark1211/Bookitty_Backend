package capstone.bookitty.domain.dto.memberDto;

import capstone.bookitty.domain.annotation.ValidEnum;
import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MemberSaveRequest(
    @NotBlank(message = "Email is a required entry value.")
    @Email(message = "Email format is not valid.")
    String email,
    @NotBlank(message = "Password is a required entry value.")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
            message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
    String password,
    @ValidEnum(enumClass = Gender.class, message = "Invalid gender")
    Gender gender, //MALE 혹은 FEMALE
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate birthdate,
    @NotBlank(message = "name is a required entry value.")
    String name
) {
    public static MemberSaveRequest of(String email, String password, Gender gender, LocalDate birthDate, String name){
        return new MemberSaveRequest(email,password,gender,birthDate,name);
    }

    public static MemberSaveRequest from(Member member){
        return new MemberSaveRequest(
                member.getEmail(),
                member.getPassword(),
                member.getGender(),
                member.getBirthDate(),
                member.getName()
        );
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password)
                .gender(gender)
                .birthDate(birthdate)
                .name(name)
                .build();
    }

    /*테스트용*/
    public static MemberSaveRequest buildForTest(String email, String password, String name){
        return new MemberSaveRequest(email,password,Gender.MALE,
                LocalDate.of(1999,2,3),name);
    }
}
