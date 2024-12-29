package capstone.bookitty.domain.dto.commonDto;

public record BoolResponse(
        boolean isUnique
) {
    public static BoolResponse of(boolean isUnique){ return new BoolResponse(isUnique); }
}
