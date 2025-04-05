package ru.yandex.practicum.yaShop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * PaymentResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-03T20:00:24.422504400+03:00[Europe/Moscow]", comments = "Generator version: 7.12.0")
public class PaymentResponse {

  private @Nullable Boolean error;

  private @Nullable String message;

  public PaymentResponse error(Boolean error) {
    this.error = error;
    return this;
  }

  /**
   * Ошибка операции
   * @return error
   */
  
  @Schema(name = "error", example = "false", description = "Ошибка операции", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("error")
  public Boolean getError() {
    return error;
  }

  public void setError(Boolean error) {
    this.error = error;
  }

  public PaymentResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Сообщение об операции
   * @return message
   */
  
  @Schema(name = "message", example = "Операция прошла успешно", description = "Сообщение об операции", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentResponse paymentResponse = (PaymentResponse) o;
    return Objects.equals(this.error, paymentResponse.error) &&
        Objects.equals(this.message, paymentResponse.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(error, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentResponse {\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

