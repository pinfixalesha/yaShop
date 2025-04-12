package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.dto.BalanceResponse;
import ru.yandex.practicum.yaShop.dto.PaymentHealthResponse;
import ru.yandex.practicum.yaShop.dto.PaymentRequest;
import ru.yandex.practicum.yaShop.dto.PaymentResponse;

@Service
public class PaymentClientService {

    @Autowired
    private WebClient paymentClient;

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private Mono<String> getAccessToken() {
        return Mono.fromCallable(this::authorizeAndGetAccessToken);
    }

    private String authorizeAndGetAccessToken() {
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId("payment")
                        .principal("system") // Используем системный принципал
                        .build()
        );

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new RuntimeException("Не удалось получить токен OAuth2");
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }

    public Mono<BalanceResponse> getBalance(Long userId) {
        return getAccessToken()
                .flatMap(token -> paymentClient.get()
                        .uri("/balance/{userId}", userId)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(BalanceResponse.class))
                .switchIfEmpty(Mono.just(new BalanceResponse().balance(0.0)))
                .onErrorResume(e -> Mono.just(new BalanceResponse().balance(0.0)));
    }

    public Mono<PaymentResponse> processPayment(Long userId, PaymentRequest paymentRequest) {
        return getAccessToken()
                .flatMap(token -> paymentClient.post()
                        .uri("/payment/{userId}", userId)
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(paymentRequest)
                        .retrieve()
                        .bodyToMono(PaymentResponse.class))
                .switchIfEmpty(Mono.just(new PaymentResponse()
                        .error(true)
                        .message("Оплата не удалась")))
                .onErrorResume(e -> Mono.just(new PaymentResponse()
                        .error(true)
                        .message("Оплата не удалась")));
    }

    public Mono<PaymentHealthResponse> checkHealth() {
        return paymentClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(PaymentHealthResponse.class)
                .onErrorResume(e -> Mono.just(PaymentHealthResponse
                        .builder()
                        .status("DOWN")
                        .build()));
    }}
