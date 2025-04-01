package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.OrderService;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerServices customerServices;

    @GetMapping("/{id}")
    public Mono<Rendering> getOrder(@PathVariable(name = "id") Long id,
                           @RequestParam(required = false, name = "newOrder") Boolean newOrder) {
        return orderService.getOrderById(id)
                .map(orderModel -> Rendering.view("order")
                        .modelAttribute("order", orderModel)
                        .modelAttribute("newOrder", Optional.ofNullable(newOrder).orElse(false))
                        .build());
    }

    @GetMapping
    public Mono<Rendering> getOrders() {
        return customerServices.getCustomer()
                .flatMapMany(customerId -> orderService.getOrdersByCustomer(customerId))
                .collectList()
                .map(ordersModel -> Rendering.view("orders")
                        .modelAttribute("orders", ordersModel)
                        .build());
    }
}
