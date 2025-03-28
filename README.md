
## Описание проекта

Это веб-приложение представляет собой витрину товаров, где пользователь может просматривать товары, добавлять их в корзину и оформлять заказы. Приложение реализовано с использованием Spring Boot и поддерживает функциональность для управления товарами, корзиной покупателя и заказами.

---

## Требования к системе

### Технические требования:
1. **Язык программирования**: Java 21
2. **Фреймворк**: Spring Boot + WebFlux
3. **Система сборки**: Gradle
4. **База данных**: Персистентная (PostgreSQL)
5. **Веб-сервер**: Netty
6. **Тестирование**: JUnit 5, TestContext Framework, Spring Boot Test
7. **Контейнеризация**: Docker

---

## Функциональность

Веб-приложение представляет собой витрину товаров, которые пользователь может положить в корзину и купить. Приложение состоит из шести основных частей (модулей):

1. **Страница витрины товаров** — веб-страница (html + javascript), на которой представлены:
    - Список товаров, доступных для заказа (картинка, название, цена, кнопка добавления в корзину/удаления из неё, кнопка изменения количества товара в корзине).
    - Список товаров может быть представлен в любом виде (списком, плиткой).
    - Пагинация (по 10, 20, 50, 100 товаров).
    - Строка поиска с фильтрацией по названию (можно просто по вхождению слова в название/описание товара).
    - Фильтрация по цене, алфавиту.

2. **Страница товара** — веб-страница, на которой представлены:
    - Название, картинка, описание товара.
    - Возможность положить товар в корзину/удалить его, изменить количество в корзине.
    - Цена товара.

3. **Страница корзины покупателя** — веб-страница, на которой представлены:
    - Список положенных в корзину товаров, их количество, цена каждого товара и общая цена всей корзины.
    - Возможность удалить товар из корзины, изменить его количество.
    - Кнопка оформления заказа.

4. **Страница всех заказов** — веб-страница, на которой представлены:
    - Список всех оформленных заказов, сумма каждого заказа и общая сумма всех заказов.
    - При нажатии на заказ появляется веб-страница совершённого заказа.

5. **Страница заказа** — веб-страница, на которой представлен список купленных товаров (картинка, название, цена).

6. **Сервис покупки** — эмуляция оформления заказа, при этом происходит переход на страницу оформленного заказа.

### Дополнительная функциональность

- Возможность загрузки списка товаров на витрину товаров (например, через веб-интерфейс добавления товара, импорт из файла и т.д.).

---

## Установка и запуск приложения

### Клонирование репозитория

```bash
git clone https://github.com/pinfixalesha/yaShop.git 
```

### Развертывание PostgreSQL в Docker

1. Выполните следующую команду для запуска PostgreSQL
```bash
docker-compose up -d 
```
2. Проверка работы PostgreSQL
```bash
docker ps 
```
Необхдоимо убедиться в наличии контейнера с именем postgres-db в списке активных контейнеров.

## Запуск приложения

1. Соберите приложение с помощью Gradle:
   ```bash
   gradle build
   ```
2. Запустите приложение:
   ```bash
   java -jar build/libs/yaShop-0.0.1-SNAPSHOT.jar
   ```

Приложение будет доступно по адресу: http://localhost:8080

## Миграция таблиц БД PostgreSQL

В проекте используется Liquibase для управления миграциями базы данных.
[db.changelog-master.yaml](src%2Fmain%2Fresources%2Fdb%2Fchangelog%2Fdb.changelog-master.yaml)

## Загрузка товаров в базу данных PostgreSQL

Этот проект позволяет загружать список товаров из файла CSV в базу данных через веб-интерфейс.

Приложение будет доступно по адресу: http://localhost:8080/upload-csv

### Инструкция по использованию
1. Откройте браузер и перейдите по адресу: http://localhost:8080/upload-csv .
2. На странице загрузки выберите файл CSV, содержащий список товаров.
3. Нажмите кнопку "Загрузить". После успешной загрузки вы увидите сообщение о количестве добавленных товаров.

### Пример файла CSV
 Формат файла CSV должен соответствовать следующему шаблону:

```csv
Наименование;Картинка (Base64);Описание;Цена
Товар 1;base64_picture_data_1;Описание товара 1;100.50
Товар 2;base64_picture_data_2;Описание товара 2;200.75
Товар 3;base64_picture_data_3;Описание товара 3;300.00
```

Описание полей:
- Наименование: строка, не может быть пустой.
- Картинка (Base64): строка, может быть пустой.
- Описание: строка, может быть пустой.
- Цена: число с двумя знаками после запятой, обязательно.

Пример файла см. https://github.com/pinfixalesha/yaShop/blob/main/src/main/resources/tovars.csv

## Тестирование

Приложение покрыто юнит- и интеграционными тестами с использованием JUnit 5, TestContext Framework и Spring Boot Test. Для запуска тестов выполните:

   ```bash
   gradle test
   ```