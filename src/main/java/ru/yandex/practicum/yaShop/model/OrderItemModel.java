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
public class OrderItemModel {
    private Long id;
    private Long tovarId;
    private String tovarName;
    private String picture;
    private Integer count;
    private BigDecimal price;
}
