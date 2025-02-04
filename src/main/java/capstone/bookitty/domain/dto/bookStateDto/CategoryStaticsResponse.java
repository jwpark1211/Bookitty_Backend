package capstone.bookitty.domain.dto.bookStateDto;


public record CategoryStaticsResponse (
        int literature,
        int humanities,
        int businessEconomics,
        int selfImprovement,
        int scienceTechnology,
        int etc
){
    public static CategoryStaticsResponse of(int literature, int humanities, int businessEconomics,
                                             int selfImprovement, int scienceTechnology, int etc){
        return new CategoryStaticsResponse(literature, humanities, businessEconomics,
                                            selfImprovement, scienceTechnology, etc);
    }
}
