package ru.yandex.practicum.yaShop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.yaShop.entities.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderModel {

    private Long id;
    private BigDecimal totalAmount;
    private Long customerId;
    private LocalDateTime orderDate;
    private String orderNumber;
    private List<OrderItemModel> items; // Список деталей заказа

}
