package nl.rabobank.mongo.repository;

import nl.rabobank.account.AccountType;
import nl.rabobank.mongo.document.account.AccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AccountRepository extends MongoRepository<AccountDocument, String> {
    Optional<AccountDocument> findByOwnerIdAndAccountNumberAndType(String ownerId, String accountNumber, AccountType type);
}