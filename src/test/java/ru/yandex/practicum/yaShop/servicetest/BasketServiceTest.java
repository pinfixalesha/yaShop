package ru.yandex.practicum.yaShop.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.BasketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = YaShopApplication.class)
public class BasketServiceTest {

    @MockitoBean(reset = MockReset.BEFORE)
    private TovarRepository tovarRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private BasketRepository basketRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderRepository orderRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private OrderItemRepository orderItemRepository;


    @Autowired
    private BasketService basketService;


    @Test
    void testAddToBasketNewItem() {
        Long tovarId = 1L;
        Long customerId = 1L;

        Tovar tovar = new Tovar(tovarId, "Test Title 1","base64", "Description 1", new BigDecimal("1000.00"));

        when(tovarRepository.findById(tovarId)).thenReturn(Optional.of(tovar));
        when(basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)).thenReturn(Optional.empty());

        basketService.addToBasket(tovarId, customerId);

        verify(basketRepository, times(1)).save(any(Basket.class));
    }

    @Test
    void testAddToBasketExistingItem() {
        Long tovarId = 1L;
        Long customerId = 1L;

        Tovar tovar = new Tovar(tovarId, "Test Title 1","base64", "Description 1", new BigDecimal("1000.00"));
        Basket existingBasket =  new Basket(1L, tovar, 2, customerId);

        when(basketRepository.findByTovarIdAndCustomerId(tovarId, customerId))
                .thenReturn(Optional.of(existingBasket));

        basketService.addToBasket(tovarId, customerId);

        assertEquals(3, existingBasket.getQuantity());
        verify(basketRepository, times(1)).save(existingBasket);
    }

    @Test
    void testBuy() {
        Long customerId = 1L;

        Tovar tovar1 = new Tovar(1L, "Test Title 1","base64", "Description 1", new BigDecimal("1000.00"));
        Basket basket1 = new Basket(1L, tovar1, 2, customerId);

        Tovar tovar2 = new Tovar(2L, "Test Title 2","base64", "Description 2", new BigDecimal("2000.00"));
        Basket basket2 = new Basket(2L, tovar2, 1, customerId);

        List<Basket> baskets = Arrays.asList(basket1, basket2);

        when(basketRepository.findByCustomerId(customerId)).thenReturn(baskets);

        Order savedOrder = new Order(1L,BigDecimal.valueOf(4000L),1L, LocalDateTime.now(),"111");

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Long orderId = basketService.buy(customerId);

        assertNotNull(orderId);
        assertEquals(1L, orderId);

        verify(basketRepository, times(1)).deleteAllByCustomerId(customerId);
        verify(orderRepository, times(1)).save(any());
        verify(orderItemRepository, times(1)).saveAll(anyList());
    }
}
