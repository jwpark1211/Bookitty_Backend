package capstone.bookitty.domain.member.dto;

import capstone.bookitty.domain.member.domain.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

public record MemberSaveRequest(
    @NotBlank(message = "Email is a required entry value")
    @Email(message = "Email format is not valid")
    String email,

    @NotBlank(message = "Password is a required entry value")
    String password,

    @NotNull(message = "Gender is a required entry value")
    Gender gender, //MALE 혹은 FEMALE

    @NotNull(message = "Birthdate is a required entry value")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthdate,

    @NotBlank(message = "name is a required entry value")
    String name

) {
    @Builder
    public static MemberSaveRequest of(String email, String password, Gender gender, LocalDate birthDate, String name){
        return new MemberSaveRequest(email,password,gender,birthDate,name);
    }
}
