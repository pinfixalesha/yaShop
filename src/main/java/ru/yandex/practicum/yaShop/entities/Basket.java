package ru.yandex.practicum.yaShop.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table(name = "basket", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Basket {

    @Id
    private Long id;

    @Column("tovar_id")
    private Long tovarId;

    @Column("quantity")
    private Integer quantity;

    @Column("customer_id")
    private Long customerId;
}
