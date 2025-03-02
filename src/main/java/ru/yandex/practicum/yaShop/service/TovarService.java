package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.yaShop.entities.Tovar;
import ru.yandex.practicum.yaShop.mapping.TovarMapper;
import ru.yandex.practicum.yaShop.model.TovarModel;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TovarService {

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private TovarMapper tovarMapper;

    public long getTotalTovarCount() {
        return tovarRepository.count();
    }

    public List<TovarModel> getTovarsWithPaginationAndSort(int page, int size, String sortType, String search) {

        Sort sort;
        if (sortType.equalsIgnoreCase("ALPHA")) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if (sortType.equalsIgnoreCase("PRICE")) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        if (search == null || search.isEmpty()) {
            return tovarRepository.findAll(pageable).stream()
                    .map(tovarMapper::mapToModel)
                    .collect(Collectors.toList());
        }

        return tovarRepository.findByNameContainingIgnoreCase(search, pageable).stream()
                .map(tovarMapper::mapToModel)
                .collect(Collectors.toList());
    }

    public TovarModel getTovarById(Long id) {
        return tovarRepository.findById(id)
                .map(tovarMapper::mapToModel)
                .orElse(null);
    }
}
