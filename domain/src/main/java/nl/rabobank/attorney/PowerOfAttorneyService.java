package nl.rabobank.attorney;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.account.IAccountRepository;
import nl.rabobank.exception.DuplicatePowerOfAttorneyException;
import nl.rabobank.exception.InvalidGrantException;
import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.user.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PowerOfAttorneyService {
    private final IPowerOfAttorneyRepository powerOfAttorneyRepository;
    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;

    @Transactional
    public PowerOfAttorney createPowerOfAttorney(
            String authenticatedGrantorId,
            String granteeId,
            String accountNumber,
            AccountType accountType,
            AuthorizationType authorizationType
    ) {
        if (authenticatedGrantorId.equals(granteeId)) {
            throw new InvalidGrantException("A user cannot grant Power of Attorney to themselves.");
        }

        userRepository.findById(granteeId)
                .orElseThrow(() -> new RecordNotFoundException("Grantee not found."));

        accountRepository.findByOwnerIdAndAccountNumberAndType(
                authenticatedGrantorId, accountNumber, accountType
        ).orElseThrow(() -> new RecordNotFoundException("Account not found or does not belong to the grantor."));

        powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(
                authenticatedGrantorId,
                granteeId,
                accountNumber,
                authorizationType
        ).ifPresent(existingPoa -> {
            throw new DuplicatePowerOfAttorneyException("An identical Power of Attorney grant already exists for this account and user.");
        });

        PowerOfAttorney domainPowerOfAttorney = new PowerOfAttorney(
                UUID.randomUUID().toString(),
                authenticatedGrantorId,
                granteeId,
                accountNumber,
                accountType,
                authorizationType
        );

        powerOfAttorneyRepository.save(domainPowerOfAttorney);

        return domainPowerOfAttorney;
    }

    @Transactional(readOnly = true)
    public List<PowerOfAttorney> getAccessGrantsForGrantee(String granteeId, Optional<AuthorizationType> authorizationType) {
        return authorizationType
                .map(authType -> powerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(granteeId, authType))
                .orElseGet(() -> powerOfAttorneyRepository.findByGranteeId(granteeId));
    }
}