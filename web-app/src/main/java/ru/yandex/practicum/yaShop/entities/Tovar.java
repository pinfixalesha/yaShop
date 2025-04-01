package ru.yandex.practicum.yaShop.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table(name = "tovars", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Tovar {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("picture")
    private String picture;

    @Column("description")
    private String description;

    @Column("price")
    private BigDecimal price;
}
