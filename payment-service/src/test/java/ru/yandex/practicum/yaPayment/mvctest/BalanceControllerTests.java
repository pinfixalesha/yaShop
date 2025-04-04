package ru.yandex.practicum.yaPayment.mvctest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.yaPayment.YaPaymentApplication;
import ru.yandex.practicum.yaPayment.controller.BalanceController;
import ru.yandex.practicum.yaPayment.dto.PaymentRequest;
import ru.yandex.practicum.yaPayment.entities.User;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;
import ru.yandex.practicum.yaPayment.service.PaymentService;

import java.math.BigDecimal;

@SpringBootTest(classes = YaPaymentApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureWebTestClient
public class BalanceControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceController balanceController;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
        userRepository.deleteAll().block();

        // Создание тестового товара
        User user = new User();
        user.setCustomerId(1L);
        user.setBalance(BigDecimal.valueOf(100));

        userRepository.save(user).block();
    }

    @Test
    void testSuccessfulUser() {
        Long userId = 1L;

        webTestClient.get()
                .uri("/balance/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(100);
    }

    @Test
    void testNoFoundUser() {
        Long userId = 2L;

        webTestClient.get()
                .uri("/balance/{userId}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }
}

