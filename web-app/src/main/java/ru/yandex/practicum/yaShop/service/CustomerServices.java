package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.model.UserModel;

@Service
public class CustomerServices {

    @Autowired
    private UserService userService;

    public Mono<Long> getCustomer() {
        return userService.getCurrentUser()
                .switchIfEmpty(Mono.just(UserModel.builder()
                        .customerId(0L)
                        .username(userService.UNKNOWN_USER)
                        .build()))
                .flatMap(userModel -> Mono.just(userModel.getCustomerId()));
    }
}
