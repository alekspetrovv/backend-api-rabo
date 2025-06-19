package nl.rabobank.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import nl.rabobank.exception.InsufficientFundsException;
import nl.rabobank.exception.InvalidAmountException;

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
    private BigDecimal balance;

    private final Instant createdAt;
    
    private Instant lastModifiedAt;

    public Account(String accountNumber, String ownerId, AccountType type) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.ownerId = ownerId;
        this.balance = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.lastModifiedAt = this.createdAt;
    }

    //todo this will be required in production
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive.");
        }
        this.balance = this.balance.add(amount);
        this.lastModifiedAt = Instant.now();
    }

    //todo this will be required in production
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal.");
        }
        this.balance = this.balance.subtract(amount);
        this.lastModifiedAt = Instant.now();
    }
}