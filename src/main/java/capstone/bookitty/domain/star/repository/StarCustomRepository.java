package capstone.bookitty.domain.star.repository;

import java.util.List;

public interface StarCustomRepository {
    
    List<String> findIsbnsWithMinimumRatings(int minCount);
    
    List<String> findDistinctIsbn();
}