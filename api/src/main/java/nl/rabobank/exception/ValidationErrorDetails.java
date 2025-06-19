package nl.rabobank.exception;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorDetails {
    String field;
    String message;
}