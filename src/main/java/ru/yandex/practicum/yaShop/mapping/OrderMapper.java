package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.yaShop.entities.OrderItem;
import ru.yandex.practicum.yaShop.model.OrderItemModel;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.entities.Order;

@Component
public class OrderMapper {

    public OrderModel mapToOrderModel(Order order) {
        if (order == null) {
            return null;
        }

        OrderModel orderModel = new OrderModel();
        orderModel.setId(order.getId());
        orderModel.setTotalAmount(order.getTotalAmount());
        orderModel.setCustomerId(order.getCustomerId());
        orderModel.setOrderDate(order.getOrderDate());
        orderModel.setOrderNumber(order.getOrderNumber());

        return orderModel;
    }

    public OrderItemModel mapToOrderItemModel(OrderItem orderItem) {
        if (orderItem == null || orderItem.getTovar() == null) {
            return null;
        }

        OrderItemModel orderItemModel = new OrderItemModel();
        orderItemModel.setId(orderItem.getId());
        orderItemModel.setTovarId(orderItem.getTovar().getId());
        orderItemModel.setTovarName(orderItem.getTovar().getName());
        orderItemModel.setPicture(orderItem.getTovar().getPicture());
        orderItemModel.setCount(orderItem.getQuantity());
        orderItemModel.setPrice(orderItem.getPrice());

        return orderItemModel;
    }

}
