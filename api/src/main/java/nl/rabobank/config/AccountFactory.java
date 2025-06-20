package nl.rabobank.config;


import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.account.SavingsAccount;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AccountFactory {
    private final Random random = new Random();

    public Account createAccount(AccountType accountType, String ownerId) {
        String newAccountNumber = generateNewAccountNumber();

        return switch (accountType) {
            case PAYMENT -> new PaymentAccount(newAccountNumber, ownerId, accountType);
            case SAVING -> new SavingsAccount(newAccountNumber, ownerId, accountType);
            default -> throw new IllegalArgumentException("Unsupported account type provided: " + accountType);
        };
    }

    public String generateNewAccountNumber() {
        String countryCode = "NL";
        String checkDigits = String.format("%02d", random.nextInt(100));

        String bankCode = "RABO";

        String accountId = random.ints(10, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        return countryCode + checkDigits + bankCode + accountId;
    }
}