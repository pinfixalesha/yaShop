package ru.yandex.practicum.yaPayment.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User " + userId + " not found");
    }
}
