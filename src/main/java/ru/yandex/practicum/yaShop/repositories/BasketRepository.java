package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.yaShop.entities.Basket;

@Repository
public interface BasketRepository  extends JpaRepository<Basket, Long> {
}
