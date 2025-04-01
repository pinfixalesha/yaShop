package ru.yandex.practicum.yaShop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BasketModel {
    private Long id;
    private Long tovarId;
    private String name;
    private String picture;
    private String description;
    private BigDecimal price;
    private Integer count;
}
