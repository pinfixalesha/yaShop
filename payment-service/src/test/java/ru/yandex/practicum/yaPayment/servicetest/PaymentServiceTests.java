package ru.yandex.practicum.yaPayment.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.yaPayment.YaPaymentApplication;
import ru.yandex.practicum.yaPayment.dto.BalanceResponse;
import ru.yandex.practicum.yaPayment.dto.PaymentRequest;
import ru.yandex.practicum.yaPayment.dto.PaymentResponse;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;
import ru.yandex.practicum.yaPayment.service.BalanceService;
import ru.yandex.practicum.yaPayment.entities.User;
import ru.yandex.practicum.yaPayment.service.PaymentService;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = YaPaymentApplication.class)
public class PaymentServiceTests {

    @MockitoBean(reset = MockReset.BEFORE)
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    void processPaymentTest() {
        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        user.setBalance(BigDecimal.valueOf(1000.50));
        when(userRepository.findByCustomerId(userId)).thenReturn(Flux.just(user));
        when(userRepository.save(user)).thenReturn(Mono.just(user));

        Mono<PaymentResponse> paymentResponse = paymentService.processPayment(userId,new PaymentRequest().amount(1000.0));

        assertEquals(false, paymentResponse.block().getError());

        verify(userRepository, times(1)).findByCustomerId(userId);
    }

}
