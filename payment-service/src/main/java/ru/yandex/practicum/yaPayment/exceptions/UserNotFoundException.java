package ru.yandex.practicum.yaPayment.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Integer userId) {
        super("User " + userId + " not found");
    }
}
