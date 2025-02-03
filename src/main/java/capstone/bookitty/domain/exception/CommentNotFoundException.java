package capstone.bookitty.domain.exception;

import capstone.bookitty.error.exception.EntityNotFoundException;

public class CommentNotFoundException extends EntityNotFoundException {

    public CommentNotFoundException(Long target) {
        super("Comment ID[" + target + "] is not found");
    }
}
