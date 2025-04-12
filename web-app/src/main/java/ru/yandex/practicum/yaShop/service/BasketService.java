package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.dto.PaymentRequest;
import ru.yandex.practicum.yaShop.dto.PaymentResponse;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.entities.OrderItem;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.mapping.BasketMapper;
import ru.yandex.practicum.yaShop.model.BasketModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private BasketMapper basketMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TovarService tovarService;

    @Autowired
    private PaymentClientService paymentClientService;

    public Mono<Void> addToBasket(Long tovarId, Long customerId) {
        Mono<Basket> basketMono = basketRepository
                .findByTovarIdAndCustomerId(tovarId, customerId)
                .defaultIfEmpty(new Basket());

        return basketMono.flatMap(basket -> updateOrAddBasketQuantity(basket, tovarId, customerId))
                .flatMap(basketRepository::save)
                .then();
    }

    private Mono<Basket> updateOrAddBasketQuantity(Basket basket, Long tovarId, Long customerId) {
        if (basket.getId() == null) {
            basket.setTovarId(tovarId);
            basket.setCustomerId(customerId);
            basket.setQuantity(0);
        }
        basket.setQuantity(basket.getQuantity() + 1);
        return Mono.just(basket);
    }

    public Mono<Void> deleteFromBasket(Long tovarId, Long customerId) {
        return basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)
                .flatMap(basket -> basketRepository.delete(basket).then());
    }

    public Mono<Void> removeFromBasket(Long tovarId, Long customerId) {
        return basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)
                .flatMap(basket -> {
                    if (basket.getQuantity() > 1) {
                        basket.setQuantity(basket.getQuantity() - 1);
                        return basketRepository.save(basket).then();
                    } else {
                        return deleteFromBasket(tovarId, customerId);
                    }
                });
    }


    public Flux<BasketModel> getBasketByCustomerId(Long customerId) {
        return basketRepository.findByCustomerId(customerId)
                .flatMap(basket -> tovarService.getTovarByIdWithCache(basket.getTovarId())
                    .map(tovar -> basketMapper.mapToBasketModel(basket,tovar)));
    }

    private String generateOrderNumber() {
        return "ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public BigDecimal calculateTotalAmount(List<BasketModel> basketModels) {
        return basketModels.stream()
                .map(basketModel -> basketModel.getPrice().multiply(BigDecimal.valueOf(basketModel.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Mono<Long> buy(Long customerId) {
        return getBasketByCustomerId(customerId)
            .collectList()
            .flatMap(basketModels -> processBasket(customerId, basketModels));
    }

    private Mono<Long> processBasket(Long customerId, List<BasketModel> basketModels) {
        if (basketModels.size()==0) {
            return Mono.error(new IllegalArgumentException("Корзина пуста."));
        }

        BigDecimal totalAmount=calculateTotalAmount(basketModels);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(totalAmount.doubleValue());

        return paymentClientService.processPayment(customerId, paymentRequest)
                .flatMap(paymentResponse -> processPaymentResponse(customerId, basketModels, paymentResponse));
    }

    private Mono<Long> processPaymentResponse(Long customerId, List<BasketModel> basketModels, PaymentResponse paymentResponse) {
        if (paymentResponse.getError()) {
            return Mono.error(new IllegalArgumentException(paymentResponse.getMessage()));
        }

        // Создаем новый заказ
        Order order = createOrder(customerId, calculateTotalAmount(basketModels));

        return orderRepository.save(order)
                .flatMap(savedOrder -> saveOrderItemsAndCleanBasket(savedOrder, basketModels))
                .flatMap(orderId -> basketRepository.deleteAllByCustomerId(customerId)
                        .then(Mono.just(orderId)));
    }

    private Mono<Long> saveOrderItemsAndCleanBasket(Order savedOrder, List<BasketModel> basketModels) {
        List<Mono<OrderItem>> orderItemMonos = basketModels.stream()
                .map(basket -> tovarService.getTovarByIdWithCache(basket.getTovarId())
                        .map(tovar -> createOrderItem(savedOrder, basket, tovar)))
                .toList();

        //Сначала сохраним табличную часть для расчета TotalAmount
        return Flux.concat(orderItemMonos)
                .collectList()
                .flatMap(orderItems ->  orderItemRepository.saveAll(orderItems)
                    .then(Mono.just(savedOrder.getId())));
    }

    private Order createOrder(Long customerId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(totalAmount);
        return order;
    }

    private OrderItem createOrderItem(Order savedOrder, BasketModel basket, Tovar tovar) {
        OrderItem orderItem = new OrderItem();
        orderItem.setTovarId(basket.getTovarId());
        orderItem.setOrderId(savedOrder.getId());
        orderItem.setQuantity(basket.getCount());
        orderItem.setPrice(tovar.getPrice());
        return orderItem;
    }
}
