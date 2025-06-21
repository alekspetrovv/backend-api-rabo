package nl.rabobank.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public abstract class Account {

    @Pattern(regexp = "^NL\\d{2}RABO\\d{10}$", message = "Account number must be a valid format.")
    private final String accountNumber;

    @NotBlank(message = "Owner ID cannot be null or empty.")
    private final String ownerId;
    @NotBlank(message = "Account type cannot be null.")
    private final AccountType type;

    @NotNull
    private final BigDecimal balance;

    private final Instant createdAt;
    
    private final Instant lastModifiedAt;

    public Account(String accountNumber, String ownerId, AccountType type) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.ownerId = ownerId;
        this.balance = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.lastModifiedAt = this.createdAt;
    }
}