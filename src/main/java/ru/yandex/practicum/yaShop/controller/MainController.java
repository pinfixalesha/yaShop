package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.model.PagingPageInfo;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.service.TovarService;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/main")
public class MainController {

    @Autowired
    private TovarService tovarService;

    @GetMapping("/items")
    public String mainDefault(@RequestParam(required = false, name = "search") String search,
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

        List<TovarModel> tovars=tovarService.getTovarsWithPaginationAndSort(paging.getPageNumber()-1,
                paging.getPageSize(),
                paging.getSort(),
                paging.getSearch());

        model.addAttribute("items", tovars);
        model.addAttribute("paging",paging);
        return "main";
    }


}
