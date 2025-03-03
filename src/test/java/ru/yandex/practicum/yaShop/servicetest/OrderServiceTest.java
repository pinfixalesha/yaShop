package ru.yandex.practicum.yaShop.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.OrderItem;
import ru.yandex.practicum.yaShop.mapping.OrderMapper;
import ru.yandex.practicum.yaShop.model.OrderItemModel;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.OrderService;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private OrderService orderService;

    @Test
    void testGetOrderById() {
        // Arrange
        Long orderId = 1L;

        Tovar tovar = new Tovar(1L, "Test Title 1","base64", "Description 1", new BigDecimal("500.00"));
        Order order = new Order(1L,BigDecimal.valueOf(1000L),1L, LocalDateTime.now(),"ORDER-123");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderModel orderModel = OrderModel.builder()
                .id(1L)
                .totalAmount(new BigDecimal("1000.00"))
                .customerId(1L)
                .orderDate(LocalDateTime.now())
                .orderNumber("ORDER-123")
                .build();

        when(orderMapper.mapToOrderModel(order)).thenReturn(orderModel);

        OrderItem orderItem = new OrderItem(1L,order,tovar,2,new BigDecimal("500.00"));

        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Arrays.asList(orderItem));

        OrderItemModel orderItemModel = OrderItemModel.builder()
                .id(1L)
                .tovarId(tovar.getId())
                .tovarName(tovar.getName())
                .picture(tovar.getPicture())
                .count(2)
                .price(new BigDecimal("500.00"))
                .build();

        when(orderMapper.mapToOrderItemModel(orderItem)).thenReturn(orderItemModel);

        OrderModel result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(new BigDecimal("1000.00"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getCount());

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderItemRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void testGetOrdersByCustomer() {
        Long customerId = 1L;

        Order order1 = new Order(1L,BigDecimal.valueOf(1000L),1L, LocalDateTime.now(),"ORDER-123");

        Order order2 = new Order(2L,BigDecimal.valueOf(2000L),1L, LocalDateTime.now(),"ORDER-456");

        List<Order> orders = Arrays.asList(order1, order2);

        when(orderRepository.findByCustomerId(customerId)).thenReturn(orders);

        OrderModel orderModel1 = OrderModel.builder()
                .id(1L)
                .totalAmount(new BigDecimal("1000.00"))
                .customerId(1L)
                .orderDate(LocalDateTime.now())
                .orderNumber("ORDER-123")
                .build();

        OrderModel orderModel2 = OrderModel.builder()
                .id(2L)
                .totalAmount(new BigDecimal("2000.00"))
                .customerId(1L)
                .orderDate(LocalDateTime.now())
                .orderNumber("ORDER-456")
                .build();

        when(orderMapper.mapToOrderModel(order1)).thenReturn(orderModel1);
        when(orderMapper.mapToOrderModel(order2)).thenReturn(orderModel2);

        List<OrderModel> result = orderService.getOrdersByCustomer(customerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(orderModel1, result.get(0));
        assertEquals(orderModel2, result.get(1));

        verify(orderRepository, times(1)).findByCustomerId(customerId);
    }
}
