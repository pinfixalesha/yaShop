package ru.yandex.practicum.yaShop.mvctest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.dto.PaymentResponse;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.CustomerServices;
import ru.yandex.practicum.yaShop.service.PaymentClientService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = YaShopApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureWebTestClient
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerServices customerServices;

    @MockitoBean(reset = MockReset.BEFORE)
    private PaymentClientService paymentClientService;

    private Long tovarId;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        basketRepository.deleteAll().block();
        tovarRepository.deleteAll().block();

        // Создание тестового товара
        Tovar tovar = new Tovar();
        tovar.setName("Title 123");
        tovar.setPicture("base64Data");
        tovar.setDescription("Description 123");
        tovar.setPrice(BigDecimal.valueOf(12345)); // Цена

        tovarId = tovarRepository.save(tovar).block().getId();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

   // @Test
   // void anonymousShouldBeRedirectedToLogin() throws Exception {
        // CSRF-токен
        //var authority = new SimpleGrantedAuthority("ROLE_USER");
        //var authentication = new UsernamePasswordAuthenticationToken("user", "password", List.of(authority));
        //SecurityContextHolder.getContext().setAuthentication(authentication);
//

//Первое, что мы хотим протестировать, — это анонимного (неаутентифицированного) пользователя.
        //webTestClient.get() .perform(get("/dashboard"))
         //       .andExpect(status().is3xxRedirection())
         //       .andExpect(redirectedUrlPattern("**/login"));

  //  }

    //@Test
    //@WithMockUser(username = "testuser", roles = {"USER"})
    //void shouldHaveCorrectPrincipal() {
    //    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //    assertEquals("testuser", auth.getName());
   //     assertTrue(auth.getAuthorities().stream()
   //             .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
   // }

    @Test
    //@WithMockUser(username = "testuser", roles = {"USER"})
    void buyOrder() {
        when(paymentClientService.processPayment(anyLong(),any()))
                .thenReturn(Mono.just(new PaymentResponse()
                        .error(false)
                        .message("Оплата удалась")));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "plus");

        webTestClient.post()
                .uri("/cart/items/" + tovarId)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверка содержимого корзины
        List<Basket> baskets = basketRepository.findByCustomerId(1L).collectList().block();
        assertEquals(1, baskets.size());
        assertEquals(1, baskets.get(0).getQuantity());
        assertEquals(tovarId, baskets.get(0).getTovarId());

        // Совершение покупки
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверка созданных заказов
        List<Order> orders = orderRepository.findByCustomerId(1L).collectList().block();
        assertEquals(1, orders.size());
        assertTrue(orders.get(0).getTotalAmount().compareTo(BigDecimal.valueOf(12345L)) == 0);

        // Проверка страницы заказов
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body, containsString(orders.get(0).getOrderNumber()));
                });
    }

}
