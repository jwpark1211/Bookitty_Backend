package capstone.bookitty.domain.bookState.dto;

import capstone.bookitty.domain.bookState.entity.BookState;
import capstone.bookitty.domain.bookState.entity.State;

public record StateUpdateResponse(
        Long id,
        State state
) {
    public static StateUpdateResponse of(Long id, State state){
        return new StateUpdateResponse(id, state);
    }

    public static StateUpdateResponse from(BookState state){
        return new StateUpdateResponse(state.getId(),state.getState());
    }
}
