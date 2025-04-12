package ru.yandex.practicum.yaShop.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.model.PagingPageInfo;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.model.UserModel;
import ru.yandex.practicum.yaShop.service.BasketService;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.TovarService;
import ru.yandex.practicum.yaShop.service.UserService;

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

    @Autowired
    private UserService userService;

    @GetMapping
    public Mono<Rendering> getListTovars(
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "sort") String sort,
            @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @RequestParam(required = false, name = "pageNumber") Integer pageNumber,
            ServerWebExchange exchange) {

        Mono<UserModel> userModel=userService.getCurrentUser()
                .switchIfEmpty(Mono.just(UserModel.builder()
                    .customerId(0L)
                    .username(userService.UNKNOWN_USER)
                    .build()));
        Mono<WebSession> webSession=exchange.getSession();

        return Mono.zip(userModel, webSession)
                .flatMap(tuple -> processUserAndSessionData(
                        tuple.getT1(), tuple.getT2(),
                        search, sort,
                        pageSize, pageNumber));
    }

    private Mono<Rendering> processUserAndSessionData(UserModel user,
                                                      WebSession session,
                                                      String search, String sort,
                                                      Integer pageSize, Integer pageNumber) {
        UserModel authorization = user.getUsername().equals(userService.UNKNOWN_USER) ? null : user;

        PagingPageInfo paging = session.getAttributeOrDefault("paging", new PagingPageInfo());
        updatePaging(paging, search, sort, pageSize, pageNumber);

        return customerServices.getCustomer()
                .flatMap(customerId -> fetchAndRenderTovars(customerId, paging, session, authorization));
    }


    private void updatePaging(PagingPageInfo paging, String search, String sort, Integer pageSize, Integer pageNumber) {
        paging.setSearch(Optional.ofNullable(search).orElse(paging.getSearch()));
        paging.setSort(Optional.ofNullable(sort).orElse(paging.getSort()));
        paging.setPageNumber(Optional.ofNullable(pageNumber).orElse(paging.getPageNumber()));
        paging.setPageSize(Optional.ofNullable(pageSize).orElse(paging.getPageSize()));
    }

    private Mono<Rendering> fetchAndRenderTovars(Long customerId, PagingPageInfo paging, WebSession session, UserModel authorization) {
        Mono<Long> totalItemsMono = tovarService.getTotalTovarCount();

        Flux<TovarModel> tovarsFlux = tovarService.getTovarsWithPaginationAndSort(
                paging.getPageNumber() - 1,
                paging.getPageSize(),
                paging.getSort(),
                paging.getSearch(),
                customerId
        );

        return Mono.zip(totalItemsMono, tovarsFlux.collectList())
                .flatMap(tuple -> processFetchedTovars(tuple.getT1(),
                        tuple.getT2(),
                        paging,
                        session,
                        authorization));
    }

    private Mono<Rendering> processFetchedTovars(Long totalItems,
                                         List<TovarModel> tovars,
                                         PagingPageInfo paging,
                                         WebSession session,
                                         UserModel authorization) {
        paging.setTotalItems(totalItems);
        session.getAttributes().put("paging", paging);

        return Mono.just(Rendering.view("main")
                .modelAttribute("items", tovars)
                .modelAttribute("paging", paging)
                .modelAttribute("authorization", authorization)
                .build());
    }

    @PostMapping(value = "/{id}")
    @Secured("ROLE_USER")
    public Mono<Rendering> actionBasket(@PathVariable(name = "id") Long tovarId,
                                      ServerWebExchange exchange) {
        return exchange.getFormData()
            .flatMap(formData -> {
                String action = Optional.ofNullable(formData.getFirst("action"))
                        .orElse("");

                if ("plus".equals(action)) {
                    return plusBasket(tovarId);
                } else if ("minus".equals(action)) {
                    return minusBasket(tovarId);
                } else {
                    return Mono.just(Rendering.redirectTo("/main/items")
                            .build());
                }
            });
    }

    public Mono<Rendering> plusBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.addToBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/main/items").build()));
                });
    }

    public Mono<Rendering> minusBasket(Long tovarId) {
        return customerServices.getCustomer()
                .flatMap(customerId -> {
                    Mono<Void> voidMono=basketService.removeFromBasket(tovarId, customerId);

                    return voidMono.then(Mono.just(Rendering.redirectTo("/main/items").build()));
                });
    }


}
