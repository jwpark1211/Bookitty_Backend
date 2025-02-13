package capstone.bookitty.domain.comment.exception;

import capstone.bookitty.global.error.exception.EntityNotFoundException;

public class LikeNotFoundException extends EntityNotFoundException {

    public LikeNotFoundException(Long target) {
        super("Like ID[" + target + "] is not found");
    }
}
