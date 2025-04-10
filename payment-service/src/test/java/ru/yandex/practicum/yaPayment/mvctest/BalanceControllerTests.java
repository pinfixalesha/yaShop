package ru.yandex.practicum.yaPayment.mvctest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.yaPayment.YaPaymentApplication;
import ru.yandex.practicum.yaPayment.controller.BalanceController;
import ru.yandex.practicum.yaPayment.entities.User;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.Collections;

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

        DefaultOAuth2User principal = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                Collections.singletonMap("sub", "user1"),
                "sub"
        );

        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "client-registration-id"
        );

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
                .uri("/balance/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(10000);
    }

    @Test
    void testNoFoundUser() {
        Long userId = 3L;

        DefaultOAuth2User principal = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                Collections.singletonMap("sub", "user1"),
                "sub"
        );

        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "client-registration-id"
        );

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
                .uri("/balance/{userId}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }
}

