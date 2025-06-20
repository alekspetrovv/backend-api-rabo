package nl.rabobank.account;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    PAYMENT("PAYMENT"),
    SAVING("SAVING");

    @JsonValue
    private final String value;
}