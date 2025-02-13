package capstone.bookitty.domain.comment.exception;

import capstone.bookitty.global.error.exception.EntityNotFoundException;

public class CommentNotFoundException extends EntityNotFoundException {

    public CommentNotFoundException(Long target) {
        super("Comment ID[" + target + "] is not found");
    }
}
