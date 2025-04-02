package ru.yandex.practicum.yaPayment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaPayment.api.BalanceApi;
import ru.yandex.practicum.yaPayment.dto.BalanceResponse;
import ru.yandex.practicum.yaPayment.exceptions.UserNotFoundException;
import ru.yandex.practicum.yaPayment.service.BalanceService;

@RestController
public class BalanceController implements BalanceApi {

    @Autowired
    private BalanceService balanceService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> balanceUserIdGet(Integer userId,final ServerWebExchange exchange) {
        return balanceService.getBalance(userId)
                .map(ResponseEntity::ok)
                .onErrorResume(UserNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

}
