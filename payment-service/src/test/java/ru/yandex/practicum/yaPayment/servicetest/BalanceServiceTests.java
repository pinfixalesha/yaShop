package ru.yandex.practicum.yaPayment.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaPayment.YaPaymentApplication;
import ru.yandex.practicum.yaPayment.dto.BalanceResponse;
import ru.yandex.practicum.yaPayment.repositories.UserRepository;
import ru.yandex.practicum.yaPayment.service.BalanceService;
import ru.yandex.practicum.yaPayment.entities.User;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = YaPaymentApplication.class)
public class BalanceServiceTests {

    @MockitoBean(reset = MockReset.BEFORE)
    private UserRepository userRepository;

    @Autowired
    private BalanceService balanceService;

    @Test
    void getBalanceTest() {
        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        user.setBalance(BigDecimal.valueOf(1000.50));
        when(userRepository.findByCustomerId(userId)).thenReturn(Flux.just(user));

        Mono<BalanceResponse> balanceResponse = balanceService.getBalance(userId);

        assertEquals(user.getBalance(), BigDecimal.valueOf(balanceResponse.block().getBalance()));

        verify(userRepository, times(1)).findByCustomerId(userId);
    }
}
