package ru.yandex.practicum.yaShop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.yaShop.entities.Tovar;

@Configuration
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, Tovar> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Tovar> serializer = new Jackson2JsonRedisSerializer<>(Tovar.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Tovar> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Tovar> context = builder.value(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Long> reactiveRedisTemplateLong(ReactiveRedisConnectionFactory factory) {
        GenericToStringSerializer<Long> valueSerializer = new GenericToStringSerializer<>(Long.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Long> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Long> context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
