package ru.yandex.practicum.yaShop.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.YaShopApplication;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.mapping.TovarMapper;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.service.TovarRedisCacheService;
import ru.yandex.practicum.yaShop.service.TovarService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = YaShopApplication.class)
public class TovarServiceTest {

    @MockitoBean(reset = MockReset.BEFORE)
    private TovarRepository tovarRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private BasketRepository basketRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private TovarRedisCacheService tovarRedisCacheService;

    @Autowired
    private TovarService tovarService;

    @MockitoBean(reset = MockReset.BEFORE)
    private TovarMapper tovarMapper;

    @Test
    void testGetTotalTovarCount() {

        long expectedCount = 10L;

        when(tovarRepository.count()).thenReturn(Mono.just(expectedCount));
        when(tovarRedisCacheService.getCachedTotalTovarCount()).thenReturn(Mono.empty());
        when(tovarRedisCacheService.cacheTotalTovarCount(anyLong())).thenReturn(Mono.just(true));

        Mono<Long> actualCountMono = tovarService.getTotalTovarCount();

        assertEquals(expectedCount, actualCountMono.block());
        verify(tovarRepository, times(1)).count();

    }


    @Test
    void testGetTovarById() {
        // Arrange
        Long id = 1L;
        Long customerId = 1L;

        Tovar tovar = new Tovar(id, "Test Title", "base64", "Description", new BigDecimal("1000.00"));

        when(tovarRepository.findById(id)).thenReturn(Mono.just(tovar));

        TovarModel model = TovarModel.builder()
                .id(id)
                .name("Test Title")
                .price(new BigDecimal("1000.00"))
                .description("Description")
                .count(0)
                .build();

        when(tovarMapper.mapToModel(tovar)).thenReturn(model);
        when(basketRepository.findByCustomerId(customerId)).thenReturn(Flux.just());
        when(tovarRedisCacheService.getCachedTovar(anyLong())).thenReturn(Mono.empty());
        when(tovarRedisCacheService.cacheTovar(tovar)).thenReturn(Mono.just(true));


        Mono<TovarModel> resultMono = tovarService.getTovarById(id, customerId);

        TovarModel result = resultMono.block();

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Title", result.getName());
        assertEquals(new BigDecimal("1000.00"), result.getPrice());

        verify(tovarRepository, times(1)).findById(id);
    }

    @Test
    void testGetTovarsWithPaginationAndSort() {
        int page = 1;
        int size = 10;
        String sortType = "PRICE";
        String search = "test";
        Long customerId = 1L;

        Tovar tovar1 = new Tovar(1L, "Test Title 1", "base64", "Description 1", new BigDecimal("1000.00"));
        Tovar tovar2 = new Tovar(2L, "Test Title 2", "base64", "Description 2", new BigDecimal("2000.00"));

        List<Tovar> tovarList = Arrays.asList(tovar1, tovar2);

        when(tovarRepository.findByNameContainingIgnoreCaseOrderByPrice(anyString(), anyInt(), anyInt()))
                .thenReturn(Flux.fromIterable(tovarList));

        TovarModel model1 = TovarModel.builder()
                .id(1L)
                .name("Test Title 1")
                .price(new BigDecimal("1000.00"))
                .description("Description 1")
                .count(0)
                .build();

        TovarModel model2 = TovarModel.builder()
                .id(2L)
                .name("Test Title 2")
                .price(new BigDecimal("2000.00"))
                .description("Description 2")
                .count(0)
                .build();

        when(tovarMapper.mapToModel(tovar1)).thenReturn(model1);
        when(tovarMapper.mapToModel(tovar2)).thenReturn(model2);
        when(tovarRedisCacheService.getCachedListTovar(anyString(),anyString(),anyInt(),anyInt())).thenReturn(Flux.empty());
        when(tovarRedisCacheService.cacheListTovar(anyString(),anyString(),anyInt(),anyInt(),anyList())).thenReturn(Mono.just(true));


        List<Basket> baskets = Arrays.asList(
                new Basket(1L, tovar1.getId(), 3, customerId),
                new Basket(2L, tovar2.getId(), 5, customerId)
        );

        when(basketRepository.findByCustomerId(customerId)).thenReturn(Flux.fromIterable(baskets));

        Flux<TovarModel> resultFlux = tovarService.getTovarsWithPaginationAndSort(page, size, sortType, search, customerId);

        List<TovarModel> result = resultFlux.collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(3, result.get(0).getCount());
        assertEquals(5, result.get(1).getCount());

        verify(tovarRepository, times(1)).findByNameContainingIgnoreCaseOrderByPrice(anyString(), anyInt(), anyInt());
        verify(basketRepository, times(1)).findByCustomerId(customerId);
    }


}
