package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Basket;

@Repository
public interface BasketRepository  extends R2dbcRepository<Basket, Long> {

    Mono<Basket> findByTovarIdAndCustomerId(Long tovarId, Long customerId);

    Flux<Basket> findByCustomerId(Long customerId);

    Mono<Void> deleteAllByCustomerId(Long customerId);

}
