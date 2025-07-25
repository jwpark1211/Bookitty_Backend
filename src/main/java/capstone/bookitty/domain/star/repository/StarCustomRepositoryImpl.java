package capstone.bookitty.domain.star.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StarCustomRepositoryImpl implements StarCustomRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<String> findIsbnsWithMinimumRatings(int minCount) {
        return entityManager.createQuery(
                """
                SELECT s.isbn 
                FROM Star s 
                GROUP BY s.isbn 
                HAVING COUNT(s) >= :minCount
                """, String.class)
                .setParameter("minCount", minCount)
                .getResultList();
    }
    
    @Override
    public List<String> findDistinctIsbn() {
        return entityManager.createQuery(
                "SELECT DISTINCT s.isbn FROM Star s", String.class)
                .getResultList();
    }
}