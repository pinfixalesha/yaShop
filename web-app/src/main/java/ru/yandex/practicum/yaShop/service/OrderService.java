package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.mapping.OrderMapper;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private TovarService tovarService;


    public Mono<OrderModel> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .flatMap(orderItem -> tovarService.getTovarByIdWithCache(orderItem.getTovarId())
                                .map(tovar -> orderMapper.mapToOrderItemModel(orderItem, tovar)))
                        .collectList()
                        .map(orderItemModels -> {
                            OrderModel orderModel = orderMapper.mapToOrderModel(order);
                            orderModel.setItems(orderItemModels);
                            return orderModel;
                        }));
    }

    public Flux<OrderModel> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .map(orderMapper::mapToOrderModel);
    }

}
