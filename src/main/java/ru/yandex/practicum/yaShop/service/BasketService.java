package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.entities.OrderItem;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.entities.Order;
import ru.yandex.practicum.yaShop.mapping.BasketMapper;
import ru.yandex.practicum.yaShop.model.BasketModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.OrderItemRepository;
import ru.yandex.practicum.yaShop.repositories.OrderRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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


    public void addToBasket(Long tovarId, Long customerId) {
        Basket basket = basketRepository.findByTovarIdAndCustomerId(tovarId, customerId)
                .orElse(new Basket());

        if (basket.getId() == null) {
            Tovar tovar = tovarRepository.findById(tovarId).orElse(null);
            basket.setTovar(tovar);
            basket.setCustomerId(customerId);
            basket.setQuantity(0); // Инициализируем количество
        }

        // Увеличиваем количество
        basket.setQuantity(basket.getQuantity() + 1);

        // Сохраняем корзину
        basketRepository.save(basket);
    }

    public void minusTovarToBasket(Long tovarId, Long customerId) {
        Optional<Basket> optionalBasket = basketRepository.findByTovarIdAndCustomerId(tovarId, customerId);

        if (optionalBasket.isPresent()) {
            Basket basket = optionalBasket.get();

            if (basket.getQuantity() > 1) {
                basket.setQuantity(basket.getQuantity() - 1);
                basketRepository.save(basket);
            } else {
                // Если количество становится <= 0, удаляем товар из корзины
                deleteFromBasket(tovarId,customerId);
            }
        }
    }

    public void deleteFromBasket(Long tovarId, Long customerId) {
        Optional<Basket> optionalBasket = basketRepository.findByTovarIdAndCustomerId(tovarId, customerId);

        if (optionalBasket.isPresent()) {
            Basket basket = optionalBasket.get();
            basketRepository.delete(basket);
        }
    }

    public List<BasketModel> getBasketByCustomerId(Long customerId) {
        List<Basket> baskets = basketRepository.findByCustomerId(customerId);

        return baskets.stream()
                .map(basketMapper::mapToBasketModel)
                .collect(Collectors.toList());
    }

    private String generateOrderNumber() {
        return "ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public BigDecimal calculateTotalAmount(List<Basket> basket) {
        return basket.stream()
                .map(b -> b.getTovar().getPrice().multiply(BigDecimal.valueOf(b.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Long buy(Long customerId) {
        //Получить все записи корзины для клиента
        List<Basket> baskets = basketRepository.findByCustomerId(customerId);

        if (baskets.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста.");
        }

        // Создать новый заказ
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(calculateTotalAmount(baskets));
        order = orderRepository.save(order);

        // Создать детали заказа (OrderItem)
        Order finalOrder = order;
        List<OrderItem> orderItems = baskets.stream()
                .map(basket -> {
                    Tovar tovar = basket.getTovar();
                    OrderItem orderItem = new OrderItem();
                    orderItem.setTovar(tovar);
                    orderItem.setOrder(finalOrder);
                    orderItem.setQuantity(basket.getQuantity());
                    orderItem.setPrice(tovar.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // Очистить корзину клиента
        basketRepository.deleteAllByCustomerId(customerId);

        return order.getId();
    }
}
