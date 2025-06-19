package nl.rabobank.mongo.repository;

import nl.rabobank.mongo.document.account.AccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<AccountDocument, String> {
}