package capstone.bookitty.domain.exception;

import capstone.bookitty.error.exception.EntityNotFoundException;

public class MemberNotFoundException extends EntityNotFoundException {

    public MemberNotFoundException(Long target) {
        super("Member ID[" + target + "] is not found");
    }
}
