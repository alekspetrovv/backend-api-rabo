package nl.rabobank;

import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Account Constructor Logic Tests")
class AccountConstructorTest {

    static class ConcreteAccount extends Account {
        public ConcreteAccount(String accountNumber, String ownerId, AccountType type) {
            super(accountNumber, ownerId, type);
        }
    }

    @Test
    @DisplayName("Constructor should set the provided values correctly")
    void constructor_shouldSetProvidedValues() {
        String expectedAccountNumber = "NL01RABO0123456789";
        String expectedOwnerId = "owner-123";
        AccountType expectedAccountType = AccountType.PAYMENT;

        ConcreteAccount account = new ConcreteAccount(expectedAccountNumber, expectedOwnerId, expectedAccountType);

        assertThat(account.getAccountNumber()).isEqualTo(expectedAccountNumber);
        assertThat(account.getOwnerId()).isEqualTo(expectedOwnerId);
        assertThat(account.getType()).isEqualTo(expectedAccountType);
    }

    @Test
    @DisplayName("Constructor should initialize balance to zero")
    void constructor_shouldInitializeBalanceToZero() {
        ConcreteAccount account = new ConcreteAccount("NL01RABO0123456789", "owner-123", AccountType.SAVING);

        assertThat(account.getBalance()).isNotNull();
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Constructor should initialize createdAt and lastModifiedAt timestamps")
    void constructor_shouldInitializeTimestamps() {
        Instant timeBeforeCreation = Instant.now();
        ConcreteAccount account = new ConcreteAccount("NL01RABO0123456789", "owner-123", AccountType.PAYMENT);

        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getLastModifiedAt()).isNotNull();
        assertThat(account.getLastModifiedAt()).isEqualTo(account.getCreatedAt());
        assertThat(account.getCreatedAt()).isCloseTo(timeBeforeCreation, within(5, ChronoUnit.SECONDS));
    }
}