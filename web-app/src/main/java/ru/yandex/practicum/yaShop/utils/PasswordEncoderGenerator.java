package ru.yandex.practicum.yaShop.utils;

import org.springframework.security.crypto.password.StandardPasswordEncoder;

public class PasswordEncoderGenerator {
    public static void main(String[] args) {
        StandardPasswordEncoder encoder = new StandardPasswordEncoder();
        String rawPassword = "pass";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
    }
}
