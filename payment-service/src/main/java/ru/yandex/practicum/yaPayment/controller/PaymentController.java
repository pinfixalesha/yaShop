package ru.yandex.practicum.yaPayment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaPayment.api.PaymentApi;
import ru.yandex.practicum.yaPayment.dto.PaymentRequest;
import ru.yandex.practicum.yaPayment.dto.PaymentResponse;
import ru.yandex.practicum.yaPayment.exceptions.InsufficientFundsException;
import ru.yandex.practicum.yaPayment.exceptions.UserNotFoundException;
import ru.yandex.practicum.yaPayment.service.PaymentService;

@RestController
public class PaymentController implements PaymentApi {

    @Autowired
    private PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentUserIdPost(
            Long userId,
            Mono<PaymentRequest> paymentRequest,
            ServerWebExchange exchange
    ) {
        return paymentRequest.flatMap(request ->
                paymentService.processPayment(userId, request)
                        .map(ResponseEntity::ok)
                        .onErrorResume(InsufficientFundsException.class, this::resultInsufficientFundsError)
                        .onErrorResume(UserNotFoundException.class, this::resultUserNotFoundError)
        );
    }

    private Mono<ResponseEntity<PaymentResponse>> resultInsufficientFundsError(InsufficientFundsException e) {
        PaymentResponse errorResponse = new PaymentResponse()
                .error(true)
                .message("Недостаточно средств на балансе");
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    private Mono<ResponseEntity<PaymentResponse>> resultUserNotFoundError(UserNotFoundException e) {
        return Mono.just(ResponseEntity.notFound().build());
    }
}
