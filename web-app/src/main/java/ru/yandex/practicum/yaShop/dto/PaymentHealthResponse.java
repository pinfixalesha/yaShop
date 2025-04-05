package ru.yandex.practicum.yaShop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentHealthResponse {
    private String status;
}
