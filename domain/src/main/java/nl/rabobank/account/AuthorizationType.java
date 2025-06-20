package nl.rabobank.account;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthorizationType {
    READ("READ"),
    WRITE("WRITE");

    @JsonValue
    private final String value;
}