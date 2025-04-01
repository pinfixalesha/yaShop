package ru.yandex.practicum.yaShop.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "orders", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private Long id;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("customer_id")
    private Long customerId;

    @Column("order_date")
    private LocalDateTime orderDate;

    @Column("order_number")
    private String orderNumber;

}
