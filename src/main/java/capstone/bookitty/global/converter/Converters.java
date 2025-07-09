package capstone.bookitty.global.converter;

import capstone.bookitty.domain.member.domain.Authority;
import capstone.bookitty.domain.member.domain.Gender;
import jakarta.persistence.Converter;

public class Converters {
    private Converters() {
    }

    @Converter(autoApply = false)
    public static class GenderConverter extends AbstractEnumAttributeConverter<Gender, Integer> {
    }

    @Converter(autoApply = false)
    public static class AuthorityConverter extends AbstractEnumAttributeConverter<Authority, Integer> {
    }
}
