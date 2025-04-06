package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.dto.PaymentRequest;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.entities.OrderItem;
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
    private TovarRepository tovarRepository;

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

        return basketMono.flatMap(basket -> {
            if (basket.getId() == null) {
                basket.setTovarId(tovarId);
                basket.setCustomerId(customerId);
                basket.setQuantity(0);
            }
            basket.setQuantity(basket.getQuantity() + 1);

            return basketRepository.save(basket).then();
        });
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
            .flatMap(basketModels -> {
               if (basketModels.size()==0) {
                    return Mono.error(new IllegalArgumentException("Корзина пуста."));
               }

               BigDecimal totalAmount=calculateTotalAmount(basketModels);

               PaymentRequest paymentRequest = new PaymentRequest();
               paymentRequest.setAmount(totalAmount.doubleValue());

                return paymentClientService.processPayment(customerId, paymentRequest)
                    .flatMap(paymentResponse -> {
                        if (paymentResponse.getError()) {
                            return Mono.error(new IllegalArgumentException(paymentResponse.getMessage()));
                        }

                        // Создаем новый заказ
                        Order order = new Order();
                        order.setCustomerId(customerId);
                        order.setOrderDate(LocalDateTime.now());
                        order.setOrderNumber(generateOrderNumber());
                        order.setTotalAmount(totalAmount);

                        return orderRepository.save(order)
                                .flatMap(savedOrder -> {

                                    List<Mono<OrderItem>> orderItemMonos = basketModels.stream()
                                            .map(basket -> tovarService.getTovarByIdWithCache(basket.getTovarId())
                                                    .map(tovar -> {
                                                        OrderItem orderItem = new OrderItem();
                                                        orderItem.setTovarId(basket.getTovarId());
                                                        orderItem.setOrderId(savedOrder.getId());
                                                        orderItem.setQuantity(basket.getCount());
                                                        orderItem.setPrice(tovar.getPrice());
                                                        return orderItem;
                                                    }))
                                            .toList();

                                    //Сначала сохраним табличную часть для расчета TotalAmount
                                    return Flux.concat(orderItemMonos)
                                            .collectList()
                                            .flatMap(orderItemsSave -> orderItemRepository.saveAll(orderItemsSave)
                                                    .then(orderRepository.save(order)
                                                            .map(savedOrderLast -> savedOrderLast.getId())));
                                })
                                .flatMap(orderId -> basketRepository.deleteAllByCustomerId(customerId)
                                        .then(Mono.just(orderId)));
                    });
            });
    }

}
