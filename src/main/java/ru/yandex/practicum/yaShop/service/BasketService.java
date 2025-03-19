package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.mapping.BasketMapper;
import ru.yandex.practicum.yaShop.model.BasketModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private BasketMapper basketMapper;

    public Mono<Void> addToBasket(Long tovarId, Long customerId) {
        Mono<Basket> basketMono = basketRepository
                .findByTovarIdAndCustomerId(tovarId, customerId)
                .defaultIfEmpty(new Basket());

        return basketMono.flatMap(basket -> {
            if (basket.getId() == null) {
                basket.setTovarId(tovarId);
                basket.setCustomerId(customerId);
                basket.setQuantity(0);
            }
            basket.setQuantity(basket.getQuantity() + 1);

            return basketRepository.save(basket).then();
        });
    }


    public Mono<Void> deleteFromBasket(Long tovarId, Long customerId) {
        return basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)
                .flatMap(basket -> basketRepository.delete(basket).then());
    }

    public Mono<Void> removeFromBasket(Long tovarId, Long customerId) {
        return basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)
                .flatMap(basket -> {
                    if (basket.getQuantity() > 1) {
                        basket.setQuantity(basket.getQuantity() - 1);
                        return basketRepository.save(basket).then();
                    } else {
                        return deleteFromBasket(tovarId, customerId);
                    }
                });
    }


    public Flux<BasketModel> getBasketByCustomerId(Long customerId) {
        return basketRepository.findByCustomerId(customerId)
                .flatMap(basket -> tovarRepository.findById(basket.getTovarId())
                .map(tovar -> basketMapper.mapToBasketModel(basket,tovar)));
    }


}
