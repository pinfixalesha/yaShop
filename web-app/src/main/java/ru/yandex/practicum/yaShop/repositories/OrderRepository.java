package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.yaShop.entities.Order;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Flux<Order> findByCustomerId(Long customerId);

}