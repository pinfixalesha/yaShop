package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Tovar;

import java.time.Duration;

@Service
public class TovarRedisCacheService {

    private static final String CACHE_PREFIX = "tovar:";
    private static final String TOTAL_COUNT_KEY = "totalTovarCount";
    private static final Duration TTL = Duration.ofMinutes(1);

    @Autowired
    private ReactiveRedisTemplate<String, Tovar> redisTemplate;

    @Autowired
    private ReactiveRedisTemplate<String, Long> redisLongTemplate;

    public Mono<Tovar> getCachedTovar(Long id) {
        String key = CACHE_PREFIX + id;
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> cacheTovar(Tovar tovar) {
        String key = CACHE_PREFIX + tovar.getId();
        return redisTemplate.opsForValue().set(key, tovar, TTL);
    }

    public Mono<Boolean> evictTovarFromCache(Long id) {
        String key = CACHE_PREFIX + id;
        return redisTemplate.opsForValue().delete(key);
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
