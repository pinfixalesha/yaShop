package ru.yandex.practicum.yaPayment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaPayment.dto.PaymentRequest;
import ru.yandex.practicum.yaPayment.dto.PaymentResponse;
import ru.yandex.practicum.yaPayment.exceptions.InsufficientFundsException;
import ru.yandex.practicum.yaPayment.exceptions.UserNotFoundException;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;
import ru.yandex.practicum.yaPayment.entities.User;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;


    public Mono<PaymentResponse> processPayment(Long userId, PaymentRequest paymentRequest) {
        BigDecimal amount = BigDecimal.valueOf(paymentRequest.getAmount());
        return userRepository.findByCustomerId(userId)
                .next()
                .flatMap(user -> processUserPayment(user, amount))
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)));
    }

    private Mono<PaymentResponse> processUserPayment(User user, BigDecimal amount) {
        if (user.getBalance().compareTo(amount) >= 0) {
            user.setBalance(user.getBalance().subtract(amount));
            return userRepository.save(user)
                    .thenReturn(new PaymentResponse().error(false).message("Операция прошла успешно"));
        } else {
            return Mono.error(new InsufficientFundsException());
        }
    }
}
