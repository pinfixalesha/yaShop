package ru.yandex.practicum.yaShop.mvctest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = YaShopApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureWebTestClient
public class TovarListControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderItemRepository orderItemRepository ;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();;
        orderRepository.deleteAll().block();;
        basketRepository.deleteAll().block();;
        tovarRepository.deleteAll().block();

        List<Tovar> tovars = new ArrayList<>();
        for (long id = 1L; id <= 15; id++) {
            Tovar tovar = new Tovar();
            tovar.setName("Title " + id);
            tovar.setPicture("base64Data");
            tovar.setDescription("Description " + id);
            tovar.setPrice(BigDecimal.valueOf(id * 1000L)); // Цена
            tovars.add(tovar);
        }
        tovarRepository.saveAll(tovars)
                .then()
                .block();
    }

    @Test
    void getFirstPage() {
        webTestClient.get()
                .uri("/main/items?pageNumber=1&pageSize=10&sort=PRICE")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    for (int i = 1; i <= 10; i++) {
                        assertThat(body, containsString("Title " + i));
                    }
                    assertThat(body, containsString("Страница: 1"));
                });
    }

    @Test
    void getNextPage() {
        webTestClient.get()
                .uri("/main/items?pageNumber=2&pageSize=10&sort=PRICE") // URI с параметрами
                .exchange() // Выполняем запрос
                .expectStatus().isOk() // Проверяем HTTP-статус
                .expectBody(String.class) // Получаем тело ответа как строку
                .value(body -> {
                    for (int i = 11; i <= 15; i++) {
                        org.hamcrest.MatcherAssert.assertThat(body, containsString("Title " + i));
                    }
                    org.hamcrest.MatcherAssert.assertThat(body, containsString("Страница: 2"));
                });
    }
}