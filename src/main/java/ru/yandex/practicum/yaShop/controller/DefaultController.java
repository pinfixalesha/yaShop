package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class DefaultController {

    @GetMapping
    public Mono<Rendering> mainDefault() {
        Rendering r = Rendering.redirectTo("/main/items")
                .build();
        return Mono.just(r);
    }


}
