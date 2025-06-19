package nl.rabobank.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getRequestPath(WebRequest webRequest) {
        return webRequest.getDescription(false).replace("uri=", "");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e, WebRequest webRequest) {
        String path = getRequestPath(webRequest);
        logger.error("JSON Parse Error at {}: {}", path, e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return new ErrorResponse(status.value(), status.getReasonPhrase(), e.getCause().getMessage(), path);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleRecordNotFoundException(RecordNotFoundException e, WebRequest webRequest) {
        String path = getRequestPath(webRequest);
        logger.error("Record Not Found Exception at {}: {}", path, e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;

        return new ErrorResponse(status.value(), status.getReasonPhrase(), e.getMessage(), path);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, WebRequest webRequest) {
        String path = getRequestPath(webRequest);
        logger.error("Validation failed at {}: {}", path, e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<ValidationErrorDetails> errorDetails = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationErrorDetails(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ErrorResponse(status.value(), status.getReasonPhrase(), "Validation failed", path, errorDetails);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException e, WebRequest webRequest) {
        String path = getRequestPath(webRequest);
        logger.error("User Already Exists Exception at {}: {}", path, e.getMessage());
        HttpStatus status = HttpStatus.CONFLICT;

        return new ErrorResponse(status.value(), status.getReasonPhrase(), e.getMessage(), path);
    }
}