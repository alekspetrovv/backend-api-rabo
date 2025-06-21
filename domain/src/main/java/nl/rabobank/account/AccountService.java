package nl.rabobank.account;

import lombok.RequiredArgsConstructor;
import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.user.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;
    private final AccountFactory accountFactory;

    @Transactional
    public Account createAccount(AccountType accountType, String ownerId) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new RecordNotFoundException("Owner with ID " + ownerId + " not found.");
        }

        Account domainAccount = accountFactory.createAccount(accountType, ownerId);
        return accountRepository.save(domainAccount);
    }
}