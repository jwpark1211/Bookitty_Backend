package capstone.bookitty.domain.dto.bookStateDto;

public record MonthlyStaticsResponse(
        int[] monthlyData
) {
    public static MonthlyStaticsResponse of(int[] monthlyData){
        return new MonthlyStaticsResponse(monthlyData);
    }
}
