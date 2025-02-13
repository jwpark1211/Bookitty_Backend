package capstone.bookitty.domain.bookState.dto;

import capstone.bookitty.global.annotation.ValidEnum;
import capstone.bookitty.domain.bookState.entity.State;

public record StateUpdateRequest(
        @ValidEnum(enumClass = State.class, message = "State is not valid.")
        State state
) {
        public static StateUpdateRequest of(State state){
                return new StateUpdateRequest(state);
        }
}
