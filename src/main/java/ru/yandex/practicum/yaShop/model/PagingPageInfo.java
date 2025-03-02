package ru.yandex.practicum.yaShop.model;

import lombok.Data;

@Data
public class PagingPageInfo {

    // Размер страницы
    private int pageSize = 10;

    // Номер страницы
    private int pageNumber = 1;

    // Общее количество элементов
    private long totalItems;

    //Сортировка
    private String sort="NO";

    //Фильтр
    private String search="";

    private long getTotalPages() {
        return (totalItems / pageSize) + (totalItems % pageSize > 0 ? 1 : 0);
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }

    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }

}
