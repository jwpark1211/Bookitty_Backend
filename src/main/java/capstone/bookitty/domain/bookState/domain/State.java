package capstone.bookitty.domain.bookState.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum State {
    READING("읽는 중"),
    WANT_TO_READ("읽고싶음"),
    READ_ALREADY("읽음")
    ;

    private final String state;
}
