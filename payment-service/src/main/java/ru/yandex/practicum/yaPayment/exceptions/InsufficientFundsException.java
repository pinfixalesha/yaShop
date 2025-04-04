package ru.yandex.practicum.yaPayment.exceptions;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Недостаточно средств на балансе для оплаты");
    }
}
