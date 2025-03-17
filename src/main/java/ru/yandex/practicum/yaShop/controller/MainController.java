package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.model.PagingPageInfo;
import ru.yandex.practicum.yaShop.model.TovarModel;
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

    //@Autowired
    //private BasketService basketService;

    @Autowired
    private CustomerServices customerServices;

    @GetMapping
    public Mono<Rendering> getListTovars(
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "sort") String sort,
            @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @RequestParam(required = false, name = "pageNumber") Integer pageNumber,
            ServerWebExchange exchange) {

        // Получаем сессию из ServerWebExchange
        return exchange.getSession()
                .flatMap(session -> {
                    // Извлекаем PagingPageInfo из сессии или создаем новый объект
                    PagingPageInfo paging = session.getAttributeOrDefault("paging", new PagingPageInfo());

                    // Обновляем значения PagingPageInfo
                    paging.setSearch(Optional.ofNullable(search).orElse(paging.getSearch()));
                    paging.setSort(Optional.ofNullable(sort).orElse(paging.getSort()));
                    paging.setPageNumber(Optional.ofNullable(pageNumber).orElse(paging.getPageNumber()));
                    paging.setPageSize(Optional.ofNullable(pageSize).orElse(paging.getPageSize()));

                    // Получаем ID клиента
                    return customerServices.getCustomer()
                            .flatMap(customerId -> {
                                // Получаем общее количество товаров
                                Mono<Long> totalItemsMono = tovarService.getTotalTovarCount();

                                // Получаем список товаров с пагинацией и сортировкой
                                Flux<TovarModel> tovarsFlux = tovarService.getTovarsWithPaginationAndSort(
                                        paging.getPageNumber() - 1,
                                        paging.getPageSize(),
                                        paging.getSort(),
                                        paging.getSearch(),
                                        customerId
                                );


                                // Соединяем данные (товары и общее количество)
                                return Mono.zip(totalItemsMono, tovarsFlux.collectList())
                                        .flatMap(tuple -> {
                                            Long totalItems = tuple.getT1();
                                            List<TovarModel> tovars = tuple.getT2();

                                            // Обновляем общее количество элементов в PagingPageInfo
                                            paging.setTotalItems(totalItems);

                                            // Сохраняем обновленный PagingPageInfo в сессию
                                            session.getAttributes().put("paging", paging);

                                            session.getAttributes().put("paging", paging);
                                            return session.save()
                                                    .thenReturn(Rendering.view("main")
                                                            .modelAttribute("items", tovars)
                                                            .modelAttribute("paging", paging)
                                                            .build());
                                        });
                            });
                });
    }

}
