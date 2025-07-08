package capstone.bookitty.domain.member.exception;

public class DuplicateEmailException extends RuntimeException {
   public DuplicateEmailException(String email){
       super("email is already in use:" + email);
   }
}