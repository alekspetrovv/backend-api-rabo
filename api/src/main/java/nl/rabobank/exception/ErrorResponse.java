package nl.rabobank.exception;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final int status;
    private final String message;
    private final String error;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String path;
    private final List<ValidationErrorDetails> errorDetails;

    public ErrorResponse(int status, String message, String error, String path, List<ValidationErrorDetails> errorDetails) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
        this.errorDetails = errorDetails;
    }


    public ErrorResponse(int status, String message, String error, String path) {
        this(status, message, error, path, null);
    }
}
