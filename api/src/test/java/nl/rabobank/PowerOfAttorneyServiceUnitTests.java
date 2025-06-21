package nl.rabobank;

import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.attorney.PowerOfAttorney;
import nl.rabobank.dto.CreatePowerOfAttorneyDto;
import nl.rabobank.exception.DuplicatePowerOfAttorneyException;
import nl.rabobank.exception.InvalidGrantException;
import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.mongo.document.account.AccountDocument;
import nl.rabobank.mongo.document.attorney.PowerOfAttorneyDocument;
import nl.rabobank.mongo.document.user.UserDocument;
import nl.rabobank.mongo.repository.AccountRepository;
import nl.rabobank.mongo.repository.PowerOfAttorneyRepository;
import nl.rabobank.mongo.repository.UserRepository;
import nl.rabobank.service.PowerOfAttorneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.MappingException;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerOfAttorneyService Unit Tests")
class PowerOfAttorneyServiceUnitTests {

    @Mock
    private PowerOfAttorneyRepository powerOfAttorneyRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PowerOfAttorneyService powerOfAttorneyService;

    private String grantorId;
    private String granteeId;
    private String accountNumber;
    private CreatePowerOfAttorneyDto createDto;
    private UserDocument granteeDocument;
    private AccountDocument accountDocument;
    private PowerOfAttorneyDocument poaDocument;

    @BeforeEach
    void setUp() {
        grantorId = "grantor-id-123";
        granteeId = "grantee-id-456";
        accountNumber = "NL01RABO0123456789";

        createDto = new CreatePowerOfAttorneyDto();
        createDto.setGranteeId(granteeId);
        createDto.setAccountNumber(accountNumber);
        createDto.setAccountType(AccountType.PAYMENT);
        createDto.setAuthorizationType(AuthorizationType.WRITE);

        granteeDocument = new UserDocument();
        granteeDocument.setId(granteeId);

        accountDocument = new AccountDocument();
        accountDocument.setOwnerId(grantorId);
        accountDocument.setAccountNumber(accountNumber);

        poaDocument = new PowerOfAttorneyDocument();
        poaDocument.setId("poa-id-789");
    }

    @Nested
    @DisplayName("createPowerOfAttorney Tests")
    class CreatePowerOfAttorneyTests {

        @Test
        @DisplayName("should create power of attorney successfully on happy path")
        void createPowerOfAttorney_happyPath_shouldSucceed() {
            when(userRepository.findById(granteeId)).thenReturn(Optional.of(granteeDocument));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(grantorId, accountNumber, AccountType.PAYMENT)).thenReturn(Optional.of(accountDocument));
            when(powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any())).thenReturn(Optional.empty());
            when(modelMapper.map(any(PowerOfAttorney.class), eq(PowerOfAttorneyDocument.class))).thenReturn(poaDocument);

            PowerOfAttorney result = powerOfAttorneyService.createPowerOfAttorney(grantorId, createDto);

            assertThat(result).isNotNull();
            assertThat(result.getGrantorId()).isEqualTo(grantorId);
            assertThat(result.getGranteeId()).isEqualTo(granteeId);

            ArgumentCaptor<PowerOfAttorneyDocument> captor = ArgumentCaptor.forClass(PowerOfAttorneyDocument.class);
            verify(powerOfAttorneyRepository).save(captor.capture());
            assertThat(captor.getValue()).isEqualTo(poaDocument);
        }

        @Test
        @DisplayName("should throw InvalidGrantException when grantor and grantee are the same")
        void createPowerOfAttorney_whenGrantorIsGrantee_shouldThrowInvalidGrantException() {
            createDto.setGranteeId(grantorId);

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(grantorId, createDto))
                    .isInstanceOf(InvalidGrantException.class)
                    .hasMessage("A user cannot grant Power of Attorney to themselves.");

            verifyNoInteractions(userRepository, accountRepository, powerOfAttorneyRepository, modelMapper);
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when grantee does not exist")
        void createPowerOfAttorney_whenGranteeNotFound_shouldThrowRecordNotFoundException() {
            when(userRepository.findById(granteeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(grantorId, createDto))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Grantee not found.");

            verify(accountRepository, never()).findByOwnerIdAndAccountNumberAndType(any(), any(), any());
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when account does not exist or belong to grantor")
        void createPowerOfAttorney_whenAccountNotFound_shouldThrowRecordNotFoundException() {
            when(userRepository.findById(granteeId)).thenReturn(Optional.of(granteeDocument));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(grantorId, accountNumber, AccountType.PAYMENT)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(grantorId, createDto))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Account not found or does not belong to the grantor.");

            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should throw DuplicatePowerOfAttorneyException when an identical grant already exists")
        void createPowerOfAttorney_whenGrantIsDuplicate_shouldThrowDuplicatePowerOfAttorneyException() {
            when(userRepository.findById(granteeId)).thenReturn(Optional.of(granteeDocument));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(grantorId, accountNumber, AccountType.PAYMENT)).thenReturn(Optional.of(accountDocument));
            when(powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(grantorId, granteeId, accountNumber, AuthorizationType.WRITE)).thenReturn(Optional.of(poaDocument));

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(grantorId, createDto))
                    .isInstanceOf(DuplicatePowerOfAttorneyException.class)
                    .hasMessage("An identical Power of Attorney grant already exists for this account and user.");

            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should propagate DataAccessException when save fails")
        void createPowerOfAttorney_whenSaveFails_shouldPropagateException() {
            when(userRepository.findById(granteeId)).thenReturn(Optional.of(granteeDocument));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(any(), any(), any())).thenReturn(Optional.of(accountDocument));
            when(powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any())).thenReturn(Optional.empty());
            when(modelMapper.map(any(), any())).thenReturn(poaDocument);
            when(powerOfAttorneyRepository.save(any(PowerOfAttorneyDocument.class))).thenThrow(new DataAccessResourceFailureException("DB down"));

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(grantorId, createDto))
                    .isInstanceOf(DataAccessResourceFailureException.class);
        }
    }

    @Nested
    @DisplayName("getAccessGrantsForGrantee Tests")
    class GetAccessGrantsForGranteeTests {

        @Test
        @DisplayName("should return all grants when authorization type is not specified")
        void getAccessGrants_whenAuthTypeIsEmpty_shouldReturnAllGrantsForGrantee() {
            List<PowerOfAttorneyDocument> documents = List.of(poaDocument, new PowerOfAttorneyDocument());
            when(powerOfAttorneyRepository.findByGranteeId(granteeId)).thenReturn(documents);
            when(modelMapper.map(any(PowerOfAttorneyDocument.class), eq(PowerOfAttorney.class))).thenReturn(new PowerOfAttorney(grantorId, granteeId, accountNumber, AccountType.PAYMENT, AuthorizationType.READ));

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(granteeId, Optional.empty());

            assertThat(results).hasSize(2);
            verify(powerOfAttorneyRepository).findByGranteeId(granteeId);
            verify(powerOfAttorneyRepository, never()).findByGranteeIdAndAuthorizationType(any(), any());
            verify(modelMapper, times(2)).map(any(PowerOfAttorneyDocument.class), eq(PowerOfAttorney.class));
        }

        @Test
        @DisplayName("should return filtered grants when authorization type is specified")
        void getAccessGrants_whenAuthTypeIsSpecified_shouldReturnFilteredGrants() {
            List<PowerOfAttorneyDocument> documents = List.of(poaDocument);
            when(powerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(granteeId, AuthorizationType.WRITE)).thenReturn(documents);
            when(modelMapper.map(any(PowerOfAttorneyDocument.class), eq(PowerOfAttorney.class))).thenReturn(new PowerOfAttorney(grantorId, granteeId, accountNumber, AccountType.PAYMENT, AuthorizationType.WRITE));

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(granteeId, Optional.of(AuthorizationType.WRITE));

            assertThat(results).hasSize(1);
            verify(powerOfAttorneyRepository, never()).findByGranteeId(any());
            verify(powerOfAttorneyRepository).findByGranteeIdAndAuthorizationType(granteeId, AuthorizationType.WRITE);
            verify(modelMapper, times(1)).map(any(PowerOfAttorneyDocument.class), eq(PowerOfAttorney.class));
        }

        @Test
        @DisplayName("should return an empty list when no grants are found")
        void getAccessGrants_whenNoGrantsFound_shouldReturnEmptyList() {
            when(powerOfAttorneyRepository.findByGranteeId(granteeId)).thenReturn(Collections.emptyList());

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(granteeId, Optional.empty());

            assertThat(results).isEmpty();
            verify(modelMapper, never()).map(any(), any());
        }
        
        @Test
        @DisplayName("should propagate exception when mapper fails")
        void getAccessGrants_whenMapperFails_shouldPropagateException() {
            List<PowerOfAttorneyDocument> documents = List.of(poaDocument);
            when(powerOfAttorneyRepository.findByGranteeId(granteeId)).thenReturn(documents);
            when(modelMapper.map(any(PowerOfAttorneyDocument.class), eq(PowerOfAttorney.class))).thenThrow(new MappingException(Collections.emptyList()));
            
            assertThatThrownBy(() -> powerOfAttorneyService.getAccessGrantsForGrantee(granteeId, Optional.empty()))
                .isInstanceOf(MappingException.class);
        }
    }
}