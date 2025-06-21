package nl.rabobank.attorney;

import nl.rabobank.account.AuthorizationType;

import java.util.List;
import java.util.Optional;

public interface IPowerOfAttorneyRepository {
    Optional<PowerOfAttorney> findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(
            String grantorId,
            String granteeId,
            String accountNumber,
            AuthorizationType authorizationType
    );

    PowerOfAttorney save(PowerOfAttorney powerOfAttorney);

    List<PowerOfAttorney> findByGranteeIdAndAuthorizationType(String granteeId, AuthorizationType authorizationType);

    List<PowerOfAttorney> findByGranteeId(String granteeId);
}