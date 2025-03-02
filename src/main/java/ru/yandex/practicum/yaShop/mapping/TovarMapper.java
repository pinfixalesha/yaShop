package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.model.TovarModel;

@Component
public class TovarMapper {

    public TovarModel mapToModel(Tovar tovar) {
        return TovarModel.builder()
                .id(tovar.getId())
                .price(tovar.getPrice())
                .name(tovar.getName())
                .description(tovar.getDescription())
                .picture(tovar.getPicture())
                .count(0)
                .build();
    }

}
