package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.yaShop.entities.Tovar;

@Repository
public interface TovarsRepository  extends JpaRepository<Tovar, Long> {
}
