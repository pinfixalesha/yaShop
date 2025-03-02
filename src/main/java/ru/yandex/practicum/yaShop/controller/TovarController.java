package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.TovarService;

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

    @GetMapping("/{id}")
    public String getTovar(@PathVariable(name = "id") Long id, Model model) {
        TovarModel tovar = tovarService.getTovarById(id, customerServices.getCustomer());
        model.addAttribute("item", tovar);
        return "item";
    }

    @PostMapping(value = "/{id}", params = "action=plus")
    public String plusBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.addToBasket(tovarId, customerServices.getCustomer());
        return "redirect:/items/" + tovarId;
    }

    @PostMapping(value = "/{id}", params = "action=minus")
    public String minusBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.minusTovarToBasket(tovarId, customerServices.getCustomer());
        return "redirect:/items/" + tovarId;
    }

    @PostMapping(value = "/{id}", params = "action=delete")
    public String deleteBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.deleteFromBasket(tovarId, customerServices.getCustomer());
        return "redirect:/items/" + tovarId;
    }
}
