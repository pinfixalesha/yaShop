package ru.yandex.practicum.yaShop.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerServices {

    //Заглушка, для получения текущего ID клиента. Задание не предусматривает авторизацию клиента, поэтому пока так
    public Mono<Long> getCustomer() {
        return Mono.just(1L);
    }
}
