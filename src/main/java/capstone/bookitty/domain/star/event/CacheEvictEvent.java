package capstone.bookitty.domain.star.event;

public record CacheEvictEvent(
        String cacheName,
        String key
) {
}