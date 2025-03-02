package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.util.Optional;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private TovarRepository tovarRepository;


    public void addToBasket(Long tovarId, Long customerId) {
        Basket basket = basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)
                .orElse(new Basket());

        if (basket.getId() == null) {
            Tovar tovar = tovarRepository.findById(tovarId).orElse(null);
            basket.setTovar(tovar);
            basket.setCustomerId(customerId);
            basket.setQuantity(0); // Инициализируем количество
        }

        // Увеличиваем количество
        basket.setQuantity(basket.getQuantity() + 1);

        // Сохраняем корзину
        basketRepository.save(basket);
    }

    public void minusTovarToBasket(Long tovarId, Long customerId) {
        Optional<Basket> optionalBasket = basketRepository.findByTovarIdAndCustomerId(tovarId, customerId);

        if (optionalBasket.isPresent()) {
            Basket basket = optionalBasket.get();

            if (basket.getQuantity() > 1) {
                basket.setQuantity(basket.getQuantity() - 1);
                basketRepository.save(basket);
            } else {
                // Если количество становится <= 0, удаляем товар из корзины
                deleteFromBasket(tovarId,customerId);
            }
        }
    }

    public void deleteFromBasket(Long tovarId, Long customerId) {
        Optional<Basket> optionalBasket = basketRepository.findByTovarIdAndCustomerId(tovarId, customerId);

        if (optionalBasket.isPresent()) {
            Basket basket = optionalBasket.get();
            basketRepository.delete(basket);
        }
    }
}
