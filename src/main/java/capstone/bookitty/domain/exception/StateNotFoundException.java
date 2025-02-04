package capstone.bookitty.domain.exception;

import capstone.bookitty.error.exception.EntityNotFoundException;

public class StateNotFoundException extends EntityNotFoundException {

    public StateNotFoundException(Long target) {
        super("State ID[" + target + "] is not found");
    }
}
