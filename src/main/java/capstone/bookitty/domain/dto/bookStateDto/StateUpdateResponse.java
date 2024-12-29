package capstone.bookitty.domain.dto.bookStateDto;

import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.State;

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
