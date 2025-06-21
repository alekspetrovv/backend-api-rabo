package nl.rabobank.mongo.repository;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.attorney.IPowerOfAttorneyRepository;
import nl.rabobank.attorney.PowerOfAttorney;
import nl.rabobank.mongo.document.attorney.PowerOfAttorneyDocument;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoPowerOfAttorneyRepositoryAdapter implements IPowerOfAttorneyRepository {
    private final PowerOfAttorneyRepository mongoDbPowerOfAttorneyRepository;
    private final ModelMapper modelMapper;

    @Override
    public Optional<PowerOfAttorney> findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(
            String grantorId,
            String granteeId,
            String accountNumber,
            AuthorizationType authorizationType
    ) {
        return mongoDbPowerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(
                grantorId, granteeId, accountNumber, authorizationType
        ).map(document -> modelMapper.map(document, PowerOfAttorney.class));
    }

    @Override
    public PowerOfAttorney save(PowerOfAttorney powerOfAttorney) {
        PowerOfAttorneyDocument documentToSave = modelMapper.map(powerOfAttorney, PowerOfAttorneyDocument.class);
        PowerOfAttorneyDocument savedDocument = mongoDbPowerOfAttorneyRepository.save(documentToSave);
        return modelMapper.map(savedDocument, PowerOfAttorney.class);
    }

    @Override
    public List<PowerOfAttorney> findByGranteeIdAndAuthorizationType(String granteeId, AuthorizationType authorizationType) {
        List<PowerOfAttorneyDocument> documents = mongoDbPowerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(
                granteeId, authorizationType
        );
        return documents.stream()
                .map(document -> modelMapper.map(document, PowerOfAttorney.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<PowerOfAttorney> findByGranteeId(String granteeId) {
        List<PowerOfAttorneyDocument> documents = mongoDbPowerOfAttorneyRepository.findByGranteeId(granteeId);
        return documents.stream()
                .map(document -> modelMapper.map(document, PowerOfAttorney.class))
                .collect(Collectors.toList());
    }
}