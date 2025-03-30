package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Tovar;

import java.time.Duration;
import java.util.List;

@Service
public class TovarRedisCacheService {

    private static final String CACHE_PREFIX = "tovar:";
    private static final String TOTAL_COUNT_KEY = "totalTovarCount";
    private static final Duration TTL = Duration.ofMinutes(1);

    @Autowired
    private ReactiveRedisTemplate<String, Tovar> redisTemplate;

    @Autowired
    private ReactiveRedisTemplate<String, Long> redisLongTemplate;

    private String generateCacheKey(String sortType, String namePart, int limit, int offset) {
        return CACHE_PREFIX + "limit=" + limit + ":offset=" + offset + ":sort=" + sortType + ":search=" + namePart;
    }
    public Flux<Tovar> getCachedListTovar(String sortType, String namePart, int limit, int offset) {
        String key =  generateCacheKey(sortType, namePart, limit, offset);
        return redisTemplate.opsForList().range(key,0,-1);
    }

    public Mono<Boolean> cacheListTovar(String sortType, String namePart, int limit, int offset, List<Tovar> tovars) {
        String key = generateCacheKey(sortType, namePart, limit, offset);
        return redisTemplate.opsForList().rightPushAll(key, tovars)
                .then(redisTemplate.expire(key, TTL));
    }

    public Mono<Tovar> getCachedTovar(Long id) {
        String key = CACHE_PREFIX + id;
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> cacheTovar(Tovar tovar) {
        String key = CACHE_PREFIX + tovar.getId();
        return redisTemplate.opsForValue().set(key, tovar, TTL);
    }

    public Mono<Long> getCachedTotalTovarCount() {
        return redisLongTemplate.opsForValue().get(TOTAL_COUNT_KEY);
    }

    public Mono<Boolean> cacheTotalTovarCount(Long count) {
        return redisLongTemplate.opsForValue().set(TOTAL_COUNT_KEY, count, TTL);
    }

    public Mono<Boolean> evictTotalTovarCountFromCache() {
        return redisLongTemplate.opsForValue().delete(TOTAL_COUNT_KEY);
    }
}
