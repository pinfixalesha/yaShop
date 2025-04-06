package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Mono<BalanceResponse> getBalance(Long userId) {
        return paymentClient.get()
                .uri("/balance/{userId}", userId)
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .switchIfEmpty(Mono.just(new BalanceResponse()
                        .balance(0.0)))
                .onErrorResume(e -> Mono.just(new BalanceResponse()
                        .balance(0.0)));

    }

    public Mono<PaymentResponse> processPayment(Long userId, PaymentRequest paymentRequest) {
        return paymentClient.post()
                .uri("/payment/{userId}", userId)
                .bodyValue(paymentRequest)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
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
