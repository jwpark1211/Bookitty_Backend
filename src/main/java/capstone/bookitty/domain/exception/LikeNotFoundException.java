package capstone.bookitty.domain.exception;

import capstone.bookitty.error.exception.EntityNotFoundException;

public class LikeNotFoundException extends EntityNotFoundException {

    public LikeNotFoundException(Long target) {
        super("Like ID[" + target + "] is not found");
    }
}
