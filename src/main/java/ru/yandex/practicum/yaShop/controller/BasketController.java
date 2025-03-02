package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.yaShop.model.BasketModel;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.TovarService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/cart/items")
public class BasketController {

    @Autowired
    private BasketService basketService;

    @Autowired
    private CustomerServices customerServices;

    @GetMapping
    public String getBasket(Model model) {
        List<BasketModel> basketModels=basketService.getBasketByCustomerId(customerServices.getCustomer());

        model.addAttribute("items", basketModels);
        model.addAttribute("total", calculateTotalAmount(basketModels));
        if (basketModels.size()==0) {
            model.addAttribute("empty", true);
        } else {
            model.addAttribute("empty", false);
        }
        return "cart";
    }

    @PostMapping(value = "/{id}", params = "action=plus")
    public String plusBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.addToBasket(tovarId, customerServices.getCustomer());
        return "redirect:/cart/items";
    }

    @PostMapping(value = "/{id}", params = "action=minus")
    public String minusBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.minusTovarToBasket(tovarId, customerServices.getCustomer());
        return "redirect:/cart/items";
    }

    @PostMapping(value = "/{id}", params = "action=delete")
    public String deleteBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.deleteFromBasket(tovarId, customerServices.getCustomer());
        return "redirect:/cart/items";
    }

    public BigDecimal calculateTotalAmount(List<BasketModel> basketModels) {
        return basketModels.stream()
                .map(basketModel -> basketModel.getPrice().multiply(BigDecimal.valueOf(basketModel.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
