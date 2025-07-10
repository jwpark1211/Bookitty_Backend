package capstone.bookitty.domain.member.domain.type;

import capstone.bookitty.global.converter.PersistableEnum;

public enum Gender implements PersistableEnum<Integer> {
    MALE(0), FEMALE(1);
    private final int value;

    Gender(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}

