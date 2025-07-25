package capstone.bookitty.domain.member.exception;

import capstone.bookitty.global.error.exception.EntityNotFoundException;

public class MemberNotFoundException extends EntityNotFoundException {

    public MemberNotFoundException(){
        super("Member not found.");
    }
    public MemberNotFoundException(Long target) {
        super("Member ID[" + target + "] is not found");
    }
    public MemberNotFoundException(String email) {
        super("Member EMAIL[" + email + "] is not found");
    }
}
