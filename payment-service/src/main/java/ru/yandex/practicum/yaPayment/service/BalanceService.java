package ru.yandex.practicum.yaPayment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaPayment.dto.BalanceResponse;
import ru.yandex.practicum.yaPayment.exceptions.UserNotFoundException;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;

@Service
public class BalanceService {

    @Autowired
    private UserRepository userRepository;

    public Mono<BalanceResponse> getBalance(Integer userId) {
        return userRepository.findByCustomerId(Long.valueOf(userId))
                .next()
                .map(user -> new BalanceResponse().balance(user.getBalance().doubleValue()))
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)));
    }
}
