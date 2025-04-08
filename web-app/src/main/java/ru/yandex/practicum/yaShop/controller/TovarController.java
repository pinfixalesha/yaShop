package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.model.UserModel;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.TovarService;
import ru.yandex.practicum.yaShop.service.UserService;

import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/items")
public class TovarController {

    @Autowired
    private TovarService tovarService;

    @Autowired
    private BasketService basketService;

    @Autowired
    private CustomerServices customerServices;

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Mono<Rendering> getTovar(@PathVariable(name = "id") Long id) {
        Mono<UserModel> userModel=userService.getCurrentUser()
                .switchIfEmpty(Mono.just(UserModel.builder()
                        .customerId(0L)
                        .username(userService.UNKNOWN_USER)
                        .build()));

        return userModel.flatMap(user -> {
            Mono<TovarModel> tovarModelMono = tovarService.getTovarById(id, user.getCustomerId());

            UserModel authorization;
            if (user.getUsername().equals(userService.UNKNOWN_USER)) {
                authorization=null;
            } else {
                authorization=user;
            }

            return tovarModelMono.map(tovarModel -> Rendering.view("item")
                    .modelAttribute("item", tovarModel) // Передаем товар в модель
                    .modelAttribute("authorization", authorization) // Передаем пользователя в модель
                    .build());
        });
    }

    @PostMapping(value = "/{id}")
    public Mono<Rendering> actionBasket(@PathVariable(name = "id") Long tovarId,
                                        ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = Optional.ofNullable(formData.getFirst("action"))
                            .orElse("");

                    if ("plus".equals(action)) {
                        return plusBasket(tovarId);
                    } else if ("minus".equals(action)) {
                        return minusBasket(tovarId);
                    } else if ("delete".equals(action)) {
                        return deleteBasket(tovarId);
                    } else {
                        return Mono.just(Rendering.redirectTo("/items/" + tovarId)
                                .build());
                    }
                });
    }

    public Mono<Rendering> deleteBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.deleteFromBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/items/" + tovarId).build()));
                });
    }

    public Mono<Rendering> plusBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.addToBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/items/" + tovarId).build()));
                });
    }

    public Mono<Rendering> minusBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.removeFromBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/items/" + tovarId).build()));
                });
    }



}
