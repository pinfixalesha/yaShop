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
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;



@SpringBootTest(classes = YaShopApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class TovarListControllerTest {

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


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        basketRepository.deleteAll();
        tovarRepository.deleteAll();

        List<Tovar> tovars = new ArrayList<>();
        for (long id = 1L; id <= 15; id++) {
            Tovar tovar = new Tovar();
            tovar.setName("Title " + id);
            tovar.setPicture("base64Data");
            tovar.setDescription("Description "+id);
            tovar.setPrice(BigDecimal.valueOf(id*1000L)); // Цена
            tovars.add(tovar);
        }
        tovarRepository.saveAll(tovars);
    }

    @Test
    void getFirstPage() throws Exception {
        mockMvc.perform(get("/main/items")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .param("sort", "PRICE"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("Страница: 1")))
                .andExpect(content().string(containsString("Title 1")))
                .andExpect(content().string(containsString("Title 2")))
                .andExpect(content().string(containsString("Title 3")))
                .andExpect(content().string(containsString("Title 4")))
                .andExpect(content().string(containsString("Title 5")))
                .andExpect(content().string(containsString("Title 6")))
                .andExpect(content().string(containsString("Title 7")))
                .andExpect(content().string(containsString("Title 8")))
                .andExpect(content().string(containsString("Title 9")))
                .andExpect(content().string(containsString("Title 10")));
    }

    @Test
    void getNextPage() throws Exception {
        mockMvc.perform(get("/main/items")
                        .param("pageNumber", "2")
                        .param("pageSize", "10")
                        .param("sort", "PRICE"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("Страница: 2")))
                .andExpect(content().string(containsString("Title 11")))
                .andExpect(content().string(containsString("Title 12")))
                .andExpect(content().string(containsString("Title 13")))
                .andExpect(content().string(containsString("Title 14")))
                .andExpect(content().string(containsString("Title 15")));
    }
}
