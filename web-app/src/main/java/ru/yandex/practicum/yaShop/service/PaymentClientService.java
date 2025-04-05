package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.dto.PaymentHealthResponse;

@Service
public class PaymentClientService {

    @Autowired
    private WebClient paymentClient;

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
