package ru.yandex.practicum.yaShop.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.yaShop.entities.Tovar;

@Repository
public interface TovarRepository extends JpaRepository<Tovar, Long> {

    //Метод для получения товаров с фильтрацией по имени, пагинацией и сортировкой.
    Page<Tovar> findByNameContainingIgnoreCase(String namePart, Pageable pageable);
}
