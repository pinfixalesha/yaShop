package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.yaShop.entities.Tovar;

@Repository
public interface TovarRepository extends R2dbcRepository<Tovar, Long> {

   @Query("SELECT * FROM tovars WHERE LOWER(name) LIKE LOWER(CONCAT('%', :namePart, '%')) ORDER BY id LIMIT :limit OFFSET :offset")
   Flux<Tovar> findByNameContainingIgnoreCaseOrderById(String namePart, int limit, int offset);

   @Query("SELECT * FROM tovars WHERE LOWER(name) LIKE LOWER(CONCAT('%', :namePart, '%')) ORDER BY name LIMIT :limit OFFSET :offset")
   Flux<Tovar> findByNameContainingIgnoreCaseOrderByName(String namePart, int limit, int offset);

   @Query("SELECT * FROM tovars WHERE LOWER(name) LIKE LOWER(CONCAT('%', :namePart, '%')) ORDER BY price LIMIT :limit OFFSET :offset")
   Flux<Tovar> findByNameContainingIgnoreCaseOrderByPrice(String namePart, int limit, int offset);

}
