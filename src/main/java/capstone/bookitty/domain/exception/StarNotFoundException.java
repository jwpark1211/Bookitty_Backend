package capstone.bookitty.domain.exception;

import capstone.bookitty.error.exception.EntityNotFoundException;

public class StarNotFoundException extends EntityNotFoundException {

    public StarNotFoundException(Long target) {
        super("Star ID[" + target + "] is not found");
    }
}
