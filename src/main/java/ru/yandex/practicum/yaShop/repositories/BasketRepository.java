package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.yaShop.entities.Basket;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository  extends JpaRepository<Basket, Long> {

    Optional<Basket> findByTovarIdAndCustomerId(Long tovarId, Long customerId);

    List<Basket> findByCustomerId(Long customerId);

    void deleteAllByCustomerId(Long customerId);
}
