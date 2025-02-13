package capstone.bookitty.domain.bookState.exception;

import capstone.bookitty.global.error.exception.EntityNotFoundException;

public class StateNotFoundException extends EntityNotFoundException {

    public StateNotFoundException(Long target) {
        super("State ID[" + target + "] is not found");
    }
}
