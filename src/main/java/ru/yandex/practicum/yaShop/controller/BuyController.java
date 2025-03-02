package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String buyBasket() {
        Long newOrderId=basketService.buy(customerServices.getCustomer());
        return "redirect:/orders/"+newOrderId+"?newOrder=true";
    }
}
