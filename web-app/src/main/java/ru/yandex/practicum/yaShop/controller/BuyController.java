package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;

@Controller
@AllArgsConstructor
@RequestMapping("/buy")
public class BuyController {

    @Autowired
    private BasketService basketService;

    @Autowired
    private CustomerServices customerServices;

    @PostMapping
    @Secured("ROLE_USER")
    public Mono<Rendering> buyBasket() {
        return customerServices.getCustomer()
                .flatMap(customerId -> basketService.buy(customerId))
                .map(newOrderId -> {
                    return Rendering.redirectTo("/orders/"+newOrderId+"?newOrder=true")
                            .build();

                });
    }
}
