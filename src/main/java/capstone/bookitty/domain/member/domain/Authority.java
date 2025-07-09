package capstone.bookitty.domain.member.domain;

import capstone.bookitty.global.converter.PersistableEnum;


public enum Authority implements PersistableEnum<Integer> {
    ROLE_ADMIN(0), ROLE_USER(1);

    private final int value;

    Authority(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
