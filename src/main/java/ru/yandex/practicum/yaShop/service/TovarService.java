package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.mapping.TovarMapper;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TovarService {

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private TovarMapper tovarMapper;

    @Autowired
    private BasketRepository basketRepository;

    public Mono<Long> getTotalTovarCount() {
        return tovarRepository.count();
    }

    public Flux<TovarModel> getTovarsWithPaginationAndSort(int page,
                                                           int size,
                                                           String sortType,
                                                           String search,
                                                           Long customerId) {
        Sort sort;
        if (sortType.equalsIgnoreCase("ALPHA")) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if (sortType.equalsIgnoreCase("PRICE")) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }
        Flux<Basket> basketFlux = basketRepository.findByCustomerId(customerId);
        int offset = page * size;

        Flux<Tovar> tovarsFlux;
        if (search == null || search.isEmpty()) {
            tovarsFlux = tovarRepository.findAllBy(sort, offset, size);
        } else {
            tovarsFlux = tovarRepository.findByNameContainingIgnoreCase(search, sort, offset, size);
        }

        return basketFlux.collectList() // Собираем корзину в список
                .flatMapMany(basket -> tovarsFlux
                        .map(tovarMapper::mapToModel) // Преобразуем товары в DTO
                        .map(tovarModel -> {
                            // Устанавливаем количество товаров в корзине
                            Integer count = basket.stream()
                                    .filter(b -> b.getTovar().getId().equals(tovarModel.getId()))
                                    .findFirst()
                                    .map(Basket::getQuantity)
                                    .orElse(0);
                            tovarModel.setCount(count);
                            return tovarModel;
                        }));


    }

}
