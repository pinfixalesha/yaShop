package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.yaShop.model.PagingInfo;
import ru.yandex.practicum.yaShop.model.TovarModel;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/main")
public class MainController {

    @GetMapping("/items")
    public String mainDefault(WebRequest request, Model model) {
        List<TovarModel> tovars=null;

        PagingInfo paging = (PagingInfo) request.getAttribute("paging", WebRequest.SCOPE_SESSION);
        if (paging == null) {
            paging = new PagingInfo();
            paging.setTotalItems(100); // Пример: всего 100 товаров
            request.setAttribute("paging", paging, WebRequest.SCOPE_SESSION);
        }

        model.addAttribute("items", tovars);
        model.addAttribute("search","");
        model.addAttribute("paging",paging);
        return "main";
    }


}
