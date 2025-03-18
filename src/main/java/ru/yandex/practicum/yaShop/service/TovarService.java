package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.mapping.TovarMapper;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.util.Optional;

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
        Flux<Basket> basketFlux = basketRepository.findByCustomerId(customerId);
        int offset = page * size;
        String repositorySearch= Optional.ofNullable(search).orElse("");

        Flux<Tovar> tovarsFlux;
        if (sortType.equalsIgnoreCase("ALPHA")) {
            tovarsFlux = tovarRepository.findByNameContainingIgnoreCaseOrderByName(repositorySearch, size, offset);
        } else if (sortType.equalsIgnoreCase("PRICE")) {
            tovarsFlux = tovarRepository.findByNameContainingIgnoreCaseOrderByPrice(repositorySearch, size, offset);
        } else {
            tovarsFlux = tovarRepository.findByNameContainingIgnoreCaseOrderById(repositorySearch, size, offset);
        }

        return basketFlux.collectList()
                .flatMapMany(basket -> tovarsFlux
                        .map(tovarMapper::mapToModel)
                        .map(tovarModel -> {
                            Integer count = basket.stream()
                                    .filter(b -> b.getTovarId().equals(tovarModel.getId()))
                                    .findFirst()
                                    .map(Basket::getQuantity)
                                    .orElse(0);
                            tovarModel.setCount(count);
                            return tovarModel;
                        }));


    }

}
