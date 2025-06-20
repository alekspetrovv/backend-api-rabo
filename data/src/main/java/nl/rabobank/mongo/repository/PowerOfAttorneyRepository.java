package nl.rabobank.mongo.repository;

import nl.rabobank.account.AuthorizationType;
import nl.rabobank.mongo.document.attorney.PowerOfAttorneyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PowerOfAttorneyRepository extends MongoRepository<PowerOfAttorneyDocument, String> {
    List<PowerOfAttorneyDocument> findByGranteeId(String granteeId);
    List<PowerOfAttorneyDocument> findByGranteeIdAndAuthorizationType(String granteeId, AuthorizationType authorizationType);
    Optional<PowerOfAttorneyDocument> findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(
            String grantorId, String granteeId, String accountNumber, AuthorizationType authorizationType);
}
