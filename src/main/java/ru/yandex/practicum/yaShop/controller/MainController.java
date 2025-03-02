package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.yaShop.model.PagingPageInfo;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.TovarService;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/main/items")
public class MainController {

    @Autowired
    private TovarService tovarService;

    @Autowired
    private BasketService basketService;

    @Autowired
    private CustomerServices customerServices;

    @GetMapping
    public String getListTovars(@RequestParam(required = false, name = "search") String search,
                                @RequestParam(required = false, name = "sort") String sort,
                                @RequestParam(required = false, name = "pageSize") Integer pageSize,
                                @RequestParam(required = false, name = "pageNumber") Integer pageNumber,
                                WebRequest request, Model model) {

        PagingPageInfo paging = (PagingPageInfo) request.getAttribute("paging", WebRequest.SCOPE_SESSION);
        if (paging == null) {
            paging = new PagingPageInfo();
            request.setAttribute("paging", paging, WebRequest.SCOPE_SESSION);
        }
        paging.setSearch(Optional.ofNullable(search).orElse(paging.getSearch()));
        paging.setSort(Optional.ofNullable(sort).orElse(paging.getSort()));
        paging.setPageNumber(Optional.ofNullable(pageNumber).orElse(paging.getPageNumber()));
        paging.setPageSize(Optional.ofNullable(pageSize).orElse(paging.getPageSize()));

        paging.setTotalItems(tovarService.getTotalTovarCount());

        List<TovarModel> tovars = tovarService.getTovarsWithPaginationAndSort(paging.getPageNumber() - 1,
                paging.getPageSize(),
                paging.getSort(),
                paging.getSearch(),
                customerServices.getCustomer());

        model.addAttribute("items", tovars);
        model.addAttribute("paging", paging);
        return "main";
    }

    @PostMapping(value = "/{id}", params = "action=plus")
    public String plusBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.addToBasket(tovarId, customerServices.getCustomer());
        return "redirect:/main/items";
    }

    @PostMapping(value = "/{id}", params = "action=minus")
    public String minusBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.minusTovarToBasket(tovarId, customerServices.getCustomer());
        return "redirect:/main/items";
    }

    @PostMapping(value = "/{id}", params = "action=delete")
    public String deleteBasket(@PathVariable(name = "id") Long tovarId) {
        basketService.deleteFromBasket(tovarId, customerServices.getCustomer());
        return "redirect:/main/items";
    }

}
