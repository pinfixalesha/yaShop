package ru.yandex.practicum.yaShop.mvctest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.CustomerServices;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = YaShopApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class OrderControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc; // Используется для отправки HTTP-запросов

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private OrderItemRepository orderItemRepository ;

    @Autowired
    private CustomerServices customerServices;

    private Long tovarId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        basketRepository.deleteAll();
        tovarRepository.deleteAll();

        Tovar tovar = new Tovar();
        tovar.setName("Title 123");
        tovar.setPicture("base64Data");
        tovar.setDescription("Description 123");
        tovar.setPrice(BigDecimal.valueOf(12345)); // Цена
        tovarRepository.save(tovar);

        tovarId=tovar.getId();
    }

    @Test
    void buyOrder() throws Exception {
        mockMvc.perform(post("/cart/items/"+tovarId)
                        .param("action","plus"))
                .andExpect(status().is3xxRedirection());

        List<Basket> baskets = basketRepository.findByCustomerId(customerServices.getCustomer());

        assertEquals(1, baskets.size());
        assertEquals(1, baskets.get(0).getQuantity());
        assertEquals(tovarId, baskets.get(0).getTovar().getId());

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection());

        List<Order> orders=orderRepository.findByCustomerId(customerServices.getCustomer());

        assertEquals(1, orders.size());
        assertTrue(orders.get(0).getTotalAmount().compareTo(BigDecimal.valueOf(12345L))==0);


        mockMvc.perform(get("/orders"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString(orders.get(0).getOrderNumber())));

    }

}
