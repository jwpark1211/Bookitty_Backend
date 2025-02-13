package capstone.bookitty.domain.member.exception;

import capstone.bookitty.global.error.exception.EntityNotFoundException;

public class MemberNotFoundException extends EntityNotFoundException {

    public MemberNotFoundException(Long target) {
        super("Member ID[" + target + "] is not found");
    }
}
