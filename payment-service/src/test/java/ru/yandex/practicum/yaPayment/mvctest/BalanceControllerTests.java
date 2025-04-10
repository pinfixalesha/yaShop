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

        User user = new User();
        user.setCustomerId(1L);
        user.setBalance(BigDecimal.valueOf(10000));
        userRepository.save(user).block();

        user = new User();
        user.setCustomerId(2L);
        user.setBalance(BigDecimal.valueOf(20000));
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
                .jsonPath("$.balance").isEqualTo(10000);
    }

    @Test
    void testNoFoundUser() {
        Long userId = 3L;

        webTestClient.get()
                .uri("/balance/{userId}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }
}

