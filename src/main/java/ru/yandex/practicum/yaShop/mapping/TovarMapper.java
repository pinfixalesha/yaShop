package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.model.TovarModel;

@Service
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
