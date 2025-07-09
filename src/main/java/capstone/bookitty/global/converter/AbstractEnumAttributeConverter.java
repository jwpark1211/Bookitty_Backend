package capstone.bookitty.global.converter;

import jakarta.persistence.AttributeConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

public abstract class AbstractEnumAttributeConverter<E extends Enum<E> & PersistableEnum<V>, V>
        implements AttributeConverter<E, V> {

    private final Class<E> enumClass;

    @SuppressWarnings("unchecked")
    public AbstractEnumAttributeConverter() {
        this.enumClass = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public V convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(V dbData) {
        if (dbData == null) return null;
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown value: " + dbData + " for enum " + enumClass.getSimpleName()));
    }
}
