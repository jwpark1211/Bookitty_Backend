package capstone.bookitty.domain.dto.bookStateDto;

import capstone.bookitty.domain.annotation.ValidEnum;
import capstone.bookitty.domain.entity.State;

public record StateUpdateRequest(
        @ValidEnum(enumClass = State.class, message = "State is not valid.")
        State state
) {
        public static StateUpdateRequest of(State state){
                return new StateUpdateRequest(state);
        }
}
