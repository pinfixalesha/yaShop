package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.dto.BalanceResponse;
import ru.yandex.practicum.yaShop.dto.PaymentHealthResponse;
import ru.yandex.practicum.yaShop.model.BasketModel;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.PaymentClientService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/cart/items")
public class BasketController {

    @Autowired
    private BasketService basketService;

    @Autowired
    private CustomerServices customerServices;

    @Autowired
    private PaymentClientService paymentClientService;


    @GetMapping
    @Secured("ROLE_USER")
    public Mono<Rendering> getBasket() {
        return customerServices.getCustomer()
            .flatMap(customerId -> {
                //баланс
                Mono<BalanceResponse> balanceMono = paymentClientService.getBalance(customerId);
                // статус сервиса
                Mono<PaymentHealthResponse> paymentHealthMono = paymentClientService.checkHealth();
                // Корзина
                Mono<List<BasketModel>> basketModelsMono = basketService.getBasketByCustomerId(customerId)
                        .collectList();
                return Mono.zip(balanceMono, paymentHealthMono, basketModelsMono)
                        .map(tuple -> {
                            BalanceResponse balance = tuple.getT1();
                            PaymentHealthResponse paymentHealth = tuple.getT2();
                            List<BasketModel> basketModels = tuple.getT3();
                            BigDecimal total=basketService.calculateTotalAmount(basketModels);

                            boolean paymentNotAvailable = !("UP".equals(paymentHealth.getStatus()));
                            boolean noMoney= (total.compareTo(new BigDecimal(balance.getBalance()))>0);
                            if (paymentNotAvailable) {
                                balance=null;
                            }
                            boolean canBuy=!(paymentNotAvailable||noMoney);

                            return Rendering.view("cart")
                                    .modelAttribute("items", basketModels)
                                    .modelAttribute("total", total)
                                    .modelAttribute("empty", basketModels.isEmpty())
                                    .modelAttribute("paymentNotAvailable", paymentNotAvailable)
                                    .modelAttribute("noMoney", noMoney)
                                    .modelAttribute("canBuy", canBuy)
                                    .modelAttribute("balance", balance)
                                    .build();
                        });
            });
    }

    @PostMapping(value = "/{id}")
    @Secured("ROLE_USER")
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
                        return Mono.just(Rendering.redirectTo("/cart/items")
                                .build());
                    }
                });
    }

    public Mono<Rendering> deleteBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.deleteFromBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/cart/items").build()));
                });
    }

    public Mono<Rendering> plusBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.addToBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/cart/items").build()));
                });
    }

    public Mono<Rendering> minusBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.removeFromBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/cart/items").build()));
                });
    }


}
