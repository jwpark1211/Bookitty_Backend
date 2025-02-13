package capstone.bookitty.domain.star.exception;

import capstone.bookitty.global.error.exception.EntityNotFoundException;

public class StarNotFoundException extends EntityNotFoundException {

    public StarNotFoundException(Long target) {
        super("Star ID[" + target + "] is not found");
    }
}
