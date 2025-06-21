package nl.rabobank.mongo.repository;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.IAccountRepository;
import nl.rabobank.mongo.document.account.AccountDocument;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoAccountRepositoryAdapter implements IAccountRepository {
    private final AccountRepository mongoDbAccountRepository;
    private final ModelMapper modelMapper;

    @Override
    public Account save(Account account) {
        AccountDocument documentToSave = modelMapper.map(account, AccountDocument.class);
        AccountDocument savedDocument = mongoDbAccountRepository.save(documentToSave);
        return modelMapper.map(savedDocument, Account.class);
    }


    @Override
    public Optional<Account> findByOwnerIdAndAccountNumberAndType(String ownerId, String accountNumber, AccountType accountType) {
        return mongoDbAccountRepository.findByOwnerIdAndAccountNumberAndType(ownerId, accountNumber, accountType.toString())
                .map(document -> modelMapper.map(document, Account.class));
    }
}