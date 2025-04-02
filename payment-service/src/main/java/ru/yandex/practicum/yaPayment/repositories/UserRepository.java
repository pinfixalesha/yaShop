package ru.yandex.practicum.yaPayment.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.yaPayment.entities.User;

@Repository
public interface UserRepository  extends R2dbcRepository<User, Long> {

    Flux<User> findByCustomerId(Long customerId);
}
