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

import java.util.List;
import java.util.Optional;

@Service
public class TovarService {

    private static final String SORT_TYPE_ALPHA = "ALPHA";
    private static final String SORT_TYPE_PRICE = "PRICE";

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private TovarMapper tovarMapper;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private TovarRedisCacheService tovarRedisCacheService;

    public Mono<Long> getTotalTovarCount() {
        return tovarRedisCacheService.getCachedTotalTovarCount()
                .switchIfEmpty(
                        tovarRepository.count()
                                .doOnSuccess(count -> tovarRedisCacheService.cacheTotalTovarCount(count).subscribe())
                );
    }

    public Flux<TovarModel> getTovarsWithPaginationAndSort(int page,
                                                           int size,
                                                           String sortType,
                                                           String search,
                                                           Long customerId) {
        Flux<Basket> basketFlux = basketRepository.findByCustomerId(customerId);
        int offset = page * size;
        String repositorySearch = Optional.ofNullable(search).orElse("");

        Flux<Tovar> tovarsFlux = getTovarListWithCache(sortType, repositorySearch, size, offset);

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

    public Mono<Tovar> getTovarByIdWithCache(Long id) {
        return tovarRedisCacheService.getCachedTovar(id)
                .switchIfEmpty(tovarRepository.findById(id)
                        .doOnSuccess(tovar -> tovarRedisCacheService.cacheTovar(tovar).subscribe()));
    }

    public Flux<Tovar> getTovarListWithCache(String sortType, String namePart, int limit, int offset) {
        return tovarRedisCacheService.getCachedListTovar(sortType, namePart, limit, offset)
                .switchIfEmpty(Flux.defer(() -> {
                    Flux<Tovar> tovarsFlux;
                    if (sortType.equalsIgnoreCase(SORT_TYPE_ALPHA)) {
                        tovarsFlux = tovarRepository.findByNameContainingIgnoreCaseOrderByName(namePart, limit, offset);
                    } else if (sortType.equalsIgnoreCase(SORT_TYPE_PRICE)) {
                        tovarsFlux = tovarRepository.findByNameContainingIgnoreCaseOrderByPrice(namePart, limit, offset);
                    } else {
                        tovarsFlux = tovarRepository.findByNameContainingIgnoreCaseOrderById(namePart, limit, offset);
                    }

                    return tovarsFlux.collectList()
                            .doOnNext(tovars ->
                                    tovarRedisCacheService.cacheListTovar(sortType, namePart, limit, offset, tovars).subscribe()
                            )
                            .flatMapMany(Flux::fromIterable);
                }));
    }

    public Mono<TovarModel> getTovarById(Long id, Long customerId) {

        Flux<Basket> basketFlux = basketRepository.findByCustomerId(customerId);

        Mono<Tovar> tovarMono = getTovarByIdWithCache(id);

        return basketFlux.collectList()
                .flatMap(basket -> tovarMono
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
