package nl.rabobank.account;

import java.util.Optional;

public interface IAccountRepository {
    Account save(Account account);
    Optional<Account> findByOwnerIdAndAccountNumberAndType(String ownerId, String accountNumber, AccountType accountType);
}