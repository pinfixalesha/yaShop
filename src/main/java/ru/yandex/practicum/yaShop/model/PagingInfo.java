package ru.yandex.practicum.yaShop.model;

import lombok.Data;

@Data
public class PagingInfo {

    private int pageSize = 10; // Размер страницы по умолчанию
    private int pageNumber = 1; // Номер страницы по умолчанию
    private int totalItems; // Общее количество элементов
    private String sort="NO"; //Сортировка по умолчанию

    private int getTotalPages() {
        return (totalItems / pageSize) + (totalItems % pageSize > 0 ? 1 : 0);
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }

    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }

}
