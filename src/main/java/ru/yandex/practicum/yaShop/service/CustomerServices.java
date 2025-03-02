package ru.yandex.practicum.yaShop.service;

import org.springframework.stereotype.Service;

@Service
public class CustomerServices {

    //Заглушка, для получения текущего ID клиента. Задание не предусматривает авторизацию клиента, поэтому пока так
    public Long getCustomer() {
        return 1L;
    }
}
