package capstone.bookitty.domain.bookState.dto;

import capstone.bookitty.domain.bookState.domain.State;

public record StateUpdateRequest(
        State state
) {
        public static StateUpdateRequest of(State state){
                return new StateUpdateRequest(state);
        }
}
