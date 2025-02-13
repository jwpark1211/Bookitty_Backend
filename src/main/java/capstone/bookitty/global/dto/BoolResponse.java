package capstone.bookitty.global.dto;

public record BoolResponse(
        boolean isUnique
) {
    public static BoolResponse of(boolean isUnique){ return new BoolResponse(isUnique); }
}
