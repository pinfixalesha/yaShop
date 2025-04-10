package ru.yandex.practicum.yaShop.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.OrderItem;
import ru.yandex.practicum.yaShop.mapping.OrderMapper;
import ru.yandex.practicum.yaShop.model.OrderItemModel;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.OrderService;
import ru.yandex.practicum.yaShop.service.TovarRedisCacheService;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = YaShopApplication.class)
public class OrderServiceTest {

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderRepository orderRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderItemRepository orderItemRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderMapper orderMapper;

    @MockitoBean(reset = MockReset.BEFORE)
    private TovarRepository tovarRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private TovarRedisCacheService tovarRedisCacheService;

    @Autowired
    private OrderService orderService;

    @Test
    void testGetOrderById() {
        Long orderId = 1L;

        Tovar tovar = new Tovar(1L, "Test Title 1", "base64", "Description 1", new BigDecimal("500.00"));
        Order order = new Order(1L, BigDecimal.valueOf(1000L), 1L, LocalDateTime.now(), "ORDER-123");

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));

        OrderModel orderModel = OrderModel.builder()
                .id(1L)
                .totalAmount(new BigDecimal("1000.00"))
                .customerId(1L)
                .orderDate(LocalDateTime.now())
                .orderNumber("ORDER-123")
                .build();


        OrderItem orderItem = new OrderItem(1L, 1L, 1L, 2, new BigDecimal("500.00"));

        OrderItemModel orderItemModel = OrderItemModel.builder()
                .id(1L)
                .tovarId(tovar.getId())
                .tovarName(tovar.getName())
                .picture(tovar.getPicture())
                .count(2)
                .price(new BigDecimal("500.00"))
                .build();

        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(orderMapper.mapToOrderModel(order)).thenReturn(orderModel);
        when(orderMapper.mapToOrderItemModel(orderItem,tovar)).thenReturn(orderItemModel);
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(tovarRepository.findById(tovar.getId())).thenReturn(Mono.just(tovar));
        when(tovarRedisCacheService.getCachedTovar(anyLong())).thenReturn(Mono.empty());
        when(tovarRedisCacheService.cacheTovar(tovar)).thenReturn(Mono.just(true));


        Mono<OrderModel> resultMono = orderService.getOrderById(orderId);

        OrderModel result = resultMono.block();

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(new BigDecimal("1000.00"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getCount());

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderItemRepository, times(1)).findByOrderId(orderId);
    }
}
