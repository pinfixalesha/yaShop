package ru.yandex.practicum.yaShop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;


@Configuration
@PropertySource("classpath:application.properties")
public class PaymentClientConfig {

    @Value("${payment.api.base-url}")
    private String userBucketPath;

    @Bean
    public WebClient webClient() {
        return WebClient.create(userBucketPath);
    }
}
