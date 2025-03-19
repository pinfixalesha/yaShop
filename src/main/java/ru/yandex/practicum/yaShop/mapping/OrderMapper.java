package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.yaShop.entities.OrderItem;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.model.OrderItemModel;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.entities.Order;

@Component
public class OrderMapper {

    public OrderModel mapToOrderModel(Order order) {
        if (order == null) {
            return null;
        }

        OrderModel orderModel = OrderModel.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate())
                .orderNumber(order.getOrderNumber())
                .build();

        return orderModel;
    }

    public OrderItemModel mapToOrderItemModel(OrderItem orderItem, Tovar tovar) {
        if (orderItem == null || tovar == null) {
            return null;
        }

        OrderItemModel orderItemModel = OrderItemModel.builder()
                .id(orderItem.getId())
                .tovarId(tovar.getId())
                .tovarName(tovar.getName())
                .picture(tovar.getPicture())
                .count(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();


        return orderItemModel;
    }

}
