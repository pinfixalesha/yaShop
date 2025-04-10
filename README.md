
## Описание проекта

- Веб-приложение представляет собой витрину товаров, где пользователь может просматривать товары, добавлять их в корзину и оформлять заказы. Приложение реализовано с использованием Spring Boot и поддерживает функциональность для управления товарами, корзиной покупателя и заказами.
- RESTful-сервис платежей к основному приложению «Витрина интернет-магазина»

---

## Требования к системе

### Технические требования:
1. **Язык программирования**: Java 21
2. **Фреймворк**: Spring Boot + WebFlux
3. **Система сборки**: Gradle
4. **База данных**: Персистентная (PostgreSQL)
5. **Кэш**: Redis
6. **Веб-сервер**: Netty
7. **Тестирование**: JUnit 5, TestContext Framework, Spring Boot Test
8. **Контейнеризация**: Docker
9. **Авторизационный сервер**: KeyCloak

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

RESTful-сервис платежей к основному приложению «Витрина интернет-магазина» представляет собой выполен на основе OpenAPI спецификации https://github.com/pinfixalesha/yaShop/blob/main/payment-service/src/main/resources/openapi.yaml обладает методами:

1. Получением баланса на счёте;
2. Осуществлением платежа.

### Безопастность

- В проекте реализована система аутентификации и авторизации с использованием Spring Security, WebFlux и Netty. 
- Для тестирования системы созданы пользователи с заранее определёнными данными. Пароли пользователей зашифрованы с использованием StandardPasswordEncoder
- Добавлена авторизация покупателя в приложении для страниц Корзина и Заказы
- Для тестирования созданы пользователи:
 
| Имя пользователя | Пароль | Роль   |
|------------------|--------|--------|
| user1            | pass   | USER   |
| user2            | pass   | USER   |
| admin            | pass   | ADMIN  |

- Авторизационный сервер KeyCloak http://localhost:8282/
Данные для авторизации 
  - KEYCLOAK_ADMIN: admin
  - KEYCLOAK_ADMIN_PASSWORD: adminpassword

 ### Дополнительная функциональность

- Возможность загрузки списка товаров на витрину товаров (например, через веб-интерфейс добавления товара, импорт из файла и т.д.).

---

## Установка и запуск приложения

### Клонирование репозитория

```bash
git clone https://github.com/pinfixalesha/yaShop.git 
```

### Развертывание PostgreSQL и Redis в Docker

1. Выполните следующую команду для запуска PostgreSQL и Redis
```bash
docker-compose up -d 
```
2. Проверка работы PostgreSQL, Redis
```bash
docker ps 
```
Необхдоимо убедиться в наличии контейнеров с именем postgres-db и redis в списке активных контейнеров.

## Запуск приложения

1. Выполните миграцию БД
   ```bash
   ./gradlew update --warning-mode all
   ```
2. Соберите приложение с помощью Gradle:
   ```bash
   gradle build
   ```
4. Запустите платежный сервис приложение:
   ```bash
   java -jar payment-service/build/libs/payment-service-0.0.3-SNAPSHOT.jar
   ```
5. Запустите web приложение:
   ```bash
   java -jar web-app/build/libs/web-app-0.0.3-SNAPSHOT.jar
   ```

Приложение будет доступно по адресу: http://localhost:8080
Платежный сервис будет доступен по адресу: http://localhost:8181

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