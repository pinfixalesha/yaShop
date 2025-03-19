package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.model.BasketModel;

@Component
public class BasketMapper {

    public BasketModel mapToBasketModel(Basket basket, Tovar tovar) {
        if (basket == null || tovar == null) {
            return null;
        }

        return BasketModel.builder()
                .id(basket.getId())
                .tovarId(tovar.getId())
                .name(tovar.getName())
                .picture(tovar.getPicture())
                .description(tovar.getDescription())
                .price(tovar.getPrice())
                .count(basket.getQuantity())
                .build();
    }
}
