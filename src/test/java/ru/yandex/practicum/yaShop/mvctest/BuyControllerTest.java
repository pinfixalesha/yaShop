package ru.yandex.practicum.yaShop.mvctest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.CustomerServices;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = YaShopApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureWebTestClient
public class BuyControllerTest {


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerServices customerServices;

    private Long tovarId;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        basketRepository.deleteAll().block();
        tovarRepository.deleteAll().block();

        // Создание тестового товара
        Tovar tovar = new Tovar();
        tovar.setName("Title 123");
        tovar.setPicture("base64Data");
        tovar.setDescription("Description 123");
        tovar.setPrice(BigDecimal.valueOf(12345)); // Цена

        tovarId = tovarRepository.save(tovar).block().getId();
    }

    @Test
    void buyOrder() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "plus");

        // Добавление товара в корзину с отправкой formData
        webTestClient.post()
                .uri("/cart/items/" + tovarId)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверка содержимого корзины
        List<Basket> baskets = basketRepository.findByCustomerId(1L).collectList().block();
        assertEquals(1, baskets.size());
        assertEquals(1, baskets.get(0).getQuantity());
        assertEquals(tovarId, baskets.get(0).getTovarId());

        // Совершение покупки
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверка созданных заказов
        List<Order> orders = orderRepository.findByCustomerId(1L).collectList().block();
        assertEquals(1, orders.size());
        assertTrue(orders.get(0).getTotalAmount().compareTo(BigDecimal.valueOf(12345L)) == 0);
    }

}
