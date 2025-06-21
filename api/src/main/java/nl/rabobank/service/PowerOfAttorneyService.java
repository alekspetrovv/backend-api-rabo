package nl.rabobank.service;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.attorney.PowerOfAttorney;
import nl.rabobank.dto.CreatePowerOfAttorneyDto;
import nl.rabobank.exception.DuplicatePowerOfAttorneyException;
import nl.rabobank.exception.InvalidGrantException;
import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.mongo.document.attorney.PowerOfAttorneyDocument;
import nl.rabobank.mongo.repository.AccountRepository;
import nl.rabobank.mongo.repository.PowerOfAttorneyRepository;
import nl.rabobank.mongo.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PowerOfAttorneyService {

    private final PowerOfAttorneyRepository powerOfAttorneyRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PowerOfAttorney createPowerOfAttorney(String authenticatedGrantorId, CreatePowerOfAttorneyDto createPowerOfAttorneyDto) {
        if (authenticatedGrantorId.equals(createPowerOfAttorneyDto.getGranteeId())) {
            throw new InvalidGrantException("A user cannot grant Power of Attorney to themselves.");
        }

        userRepository.findById(createPowerOfAttorneyDto.getGranteeId())
                .orElseThrow(() -> new RecordNotFoundException("Grantee not found."));


        accountRepository.findByOwnerIdAndAccountNumberAndType(
                authenticatedGrantorId, createPowerOfAttorneyDto.getAccountNumber(), createPowerOfAttorneyDto.getAccountType()
        ).orElseThrow(() -> new RecordNotFoundException("Account not found or does not belong to the grantor."));

        powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(
                authenticatedGrantorId,
                createPowerOfAttorneyDto.getGranteeId(),
                createPowerOfAttorneyDto.getAccountNumber(),
                createPowerOfAttorneyDto.getAuthorizationType()
        ).ifPresent(existingPoa -> {
            throw new DuplicatePowerOfAttorneyException("An identical Power of Attorney grant already exists for this account and user.");
        });

        PowerOfAttorney domainPowerOfAttorney = new PowerOfAttorney(
                authenticatedGrantorId,
                createPowerOfAttorneyDto.getGranteeId(),
                createPowerOfAttorneyDto.getAccountNumber(),
                createPowerOfAttorneyDto.getAccountType(),
                createPowerOfAttorneyDto.getAuthorizationType()
        );
        PowerOfAttorneyDocument documentToSave = modelMapper.map(domainPowerOfAttorney, PowerOfAttorneyDocument.class);
        powerOfAttorneyRepository.save(documentToSave);

        return domainPowerOfAttorney;
    }

    @Transactional(readOnly = true)
    public List<PowerOfAttorney> getAccessGrantsForGrantee(String granteeId, Optional<AuthorizationType> authorizationType) {
        List<PowerOfAttorneyDocument> poaDocuments = authorizationType
                .map(authType -> powerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(granteeId, authType))
                .orElseGet(() -> powerOfAttorneyRepository.findByGranteeId(granteeId));

        return poaDocuments.stream()
                .map(poaDoc -> modelMapper.map(poaDoc, PowerOfAttorney.class))
                .collect(Collectors.toList());
    }
}