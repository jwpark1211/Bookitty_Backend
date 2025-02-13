package capstone.bookitty.domain.bookState.dto;

public record MonthlyStaticsResponse(
        int[] monthlyData
) {
    public static MonthlyStaticsResponse of(int[] monthlyData){
        return new MonthlyStaticsResponse(monthlyData);
    }
}
