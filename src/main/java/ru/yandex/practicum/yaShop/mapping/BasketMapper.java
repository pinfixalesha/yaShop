package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.model.BasketModel;

@Component
public class BasketMapper {

    public BasketModel mapToBasketModel(Basket basket) {
        if (basket == null || basket.getTovar() == null) {
            return null;
        }

        return BasketModel.builder()
                .id(basket.getId())
                .tovarId(basket.getTovar().getId())
                .name(basket.getTovar().getName())
                .picture(basket.getTovar().getPicture())
                .description(basket.getTovar().getDescription())
                .price(basket.getTovar().getPrice())
                .count(basket.getQuantity())
                .build();
    }
}
