package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.yaShop.mapping.OrderMapper;
import ru.yandex.practicum.yaShop.model.OrderItemModel;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderMapper orderMapper;

    public OrderModel getOrderById(Long orderId) {
        OrderModel orderModel=orderRepository.findById(orderId)
                .map(orderMapper::mapToOrderModel)
                .map(o -> {
                    o.setItems(orderItemRepository.findByOrderId(o.getId()).stream()
                            .map(orderMapper::mapToOrderItemModel)
                            .collect(Collectors.toList()));
                    return o;
                })
                .orElse(null);

        return orderModel;
    }


    public List<OrderModel> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(orderMapper::mapToOrderModel)
                .collect(Collectors.toList());
    }

}
