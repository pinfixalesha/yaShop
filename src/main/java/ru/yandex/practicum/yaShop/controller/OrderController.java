package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.yaShop.model.OrderModel;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.OrderService;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerServices customerServices;

    @GetMapping("/{id}")
    public String getOrder(@PathVariable(name = "id") Long id,
                           @RequestParam(required = false, name = "newOrder") Boolean newOrder,
                           Model model) {

        OrderModel orderModel=orderService.getOrderById(id);
        model.addAttribute("order", orderModel);
        if (newOrder==null) {
            model.addAttribute("newOrder", false);
        } else {
            model.addAttribute("newOrder", newOrder);
        }
        return "order";
    }

    @GetMapping
    public String getOrders(Model model) {

        List<OrderModel> ordersModel=orderService.getOrdersByCustomer(customerServices.getCustomer());
        model.addAttribute("orders", ordersModel);
        return "orders";
    }
}
