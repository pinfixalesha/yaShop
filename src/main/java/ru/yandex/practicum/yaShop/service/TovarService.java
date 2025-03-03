package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.yaShop.entities.Basket;
import ru.yandex.practicum.yaShop.mapping.TovarMapper;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.repositories.BasketRepository;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TovarService {

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private TovarMapper tovarMapper;

    @Autowired
    private BasketRepository basketRepository;

    public long getTotalTovarCount() {
        return tovarRepository.count();
    }

    public List<TovarModel> getTovarsWithPaginationAndSort(int page,
                                                           int size,
                                                           String sortType,
                                                           String search,
                                                           Long customerId) {

        Sort sort;
        if (sortType.equalsIgnoreCase("ALPHA")) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if (sortType.equalsIgnoreCase("PRICE")) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }
        List<Basket> basket=basketRepository.findByCustomerId(customerId);

        Pageable pageable = PageRequest.of(page, size, sort);

        List<TovarModel> tovarsModel;
        if (search == null || search.isEmpty()) {
            tovarsModel=tovarRepository.findAll(pageable).stream()
                    .map(tovarMapper::mapToModel)
                    .collect(Collectors.toList());
        }

        tovarsModel=tovarRepository.findByNameContainingIgnoreCase(search, pageable).stream()
                .map(tovarMapper::mapToModel)
                .collect(Collectors.toList());

        return tovarsModel.stream()
                .map(tovarModel -> {
                    tovarModel.setCount(basket.stream()
                            .filter(b -> b.getTovar().getId().equals(tovarModel.getId()))
                            .findFirst()
                            .map(Basket::getQuantity)
                            .orElse(0));
                    return tovarModel;
                })
                .collect(Collectors.toList());
    }

    public TovarModel getTovarById(Long id,Long customerId) {

        List<Basket> basket=basketRepository.findByCustomerId(customerId);

        TovarModel tovarModel=tovarRepository.findById(id)
                .map(tovarMapper::mapToModel)
                .orElse(null);

        if (tovarModel == null) {
            return null; // Товар не найден
        }

        // Находим количество товара в корзине
        tovarModel.setCount(basket.stream()
                .filter(b -> b.getTovar().getId().equals(id))
                .findFirst()
                .map(Basket::getQuantity)
                .orElse(0));

        return tovarModel;
    }
}
