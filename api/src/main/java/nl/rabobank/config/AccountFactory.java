package nl.rabobank.config;


import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.account.SavingsAccount;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountFactory {

    public Account createAccount(AccountType accountType, String ownerId) {
        String newAccountNumber = generateNewAccountNumber();

        return switch (accountType) {
            case PAYMENT -> new PaymentAccount(newAccountNumber, ownerId, accountType);
            case SAVING -> new SavingsAccount(newAccountNumber, ownerId, accountType);
            default -> throw new IllegalArgumentException("Unsupported account type provided: " + accountType);
        };
    }

    private String generateNewAccountNumber() {
        return "NL" + String.format("%02d", (int) (Math.random() * 99)) + "RABO" +
               UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}