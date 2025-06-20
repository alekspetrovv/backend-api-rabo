package nl.rabobank.service;


import lombok.AllArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.config.AccountFactory;
import nl.rabobank.mongo.document.account.AccountDocument;
import nl.rabobank.mongo.repository.AccountRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final AccountFactory accountFactory;

    public Account createAccount(AccountType accountType, String ownerId) {
        Account domainAccount = accountFactory.createAccount(accountType, ownerId);

        AccountDocument documentToSave = modelMapper.map(domainAccount, AccountDocument.class);
        accountRepository.save(documentToSave);

        return domainAccount;
    }
}