package capstone.bookitty.global.converter;

import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.domain.type.Gender;
import jakarta.persistence.Converter;

public class EnumConverters {
    private EnumConverters() {
    }

    @Converter(autoApply = false)
    public static class GenderConverter extends AbstractEnumAttributeConverter<Gender, Integer> {
    }

    @Converter(autoApply = false)
    public static class AuthorityConverter extends AbstractEnumAttributeConverter<Authority, Integer> {
    }
}
