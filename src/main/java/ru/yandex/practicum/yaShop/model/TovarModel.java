package ru.yandex.practicum.yaShop.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TovarModel {
    private Long id;
    private String name;
    private String picture;
    private String description;
    private BigDecimal price;
    private Long count;
}
