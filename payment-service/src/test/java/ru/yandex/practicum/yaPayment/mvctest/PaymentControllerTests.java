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
import ru.yandex.practicum.yaPayment.dto.PaymentRequest;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;
import ru.yandex.practicum.yaPayment.service.PaymentService;
import ru.yandex.practicum.yaPayment.entities.User;
import java.math.BigDecimal;

@SpringBootTest(classes = YaPaymentApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureWebTestClient
public class PaymentControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

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
    void testSuccessfulPayment() {
        Long userId = 1L;
        PaymentRequest request = new PaymentRequest().amount(50.0);

        webTestClient.post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.error").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Операция прошла успешно");
    }

    @Test
    void testInsufficientPayment() {
        Long userId = 1L;
        PaymentRequest request = new PaymentRequest().amount(500.0);

        webTestClient.post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.error").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Недостаточно средств на балансе");
    }

    @Test
    void testUserNoFoundPayment() {
        Long userId = 2L;
        PaymentRequest request = new PaymentRequest().amount(100.0);

        webTestClient.post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

}

