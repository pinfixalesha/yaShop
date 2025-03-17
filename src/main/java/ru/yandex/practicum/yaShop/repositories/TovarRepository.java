package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.yaShop.entities.Tovar;
import org.springframework.data.domain.Sort;

@Repository
public interface TovarRepository extends R2dbcRepository<Tovar, Long> {

   Flux<Tovar> findAllBy(Sort sort, int offset, int limit);

   Flux<Tovar> findByNameContainingIgnoreCase(String namePart, Sort sort, int offset, int limit);

}
