package ru.yandex.practicum.yaShop.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Table(name = "order_items", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("tovar_id")
    private Long tovarId;

    @Column("quantity")
    private Integer quantity;

    @Column("price")
    private BigDecimal price;
}
