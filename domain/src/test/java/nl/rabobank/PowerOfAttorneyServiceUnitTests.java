package nl.rabobank;

import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.account.IAccountRepository;
import nl.rabobank.attorney.IPowerOfAttorneyRepository;
import nl.rabobank.attorney.PowerOfAttorney;
import nl.rabobank.attorney.PowerOfAttorneyService;
import nl.rabobank.exception.DuplicatePowerOfAttorneyException;
import nl.rabobank.exception.InvalidGrantException;
import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.user.IUserRepository;
import nl.rabobank.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerOfAttorneyService Unit Tests")
class PowerOfAttorneyServiceUnitTests {

    static class TestAccountWithNullableType extends Account {
        public TestAccountWithNullableType(String accountNumber, String ownerId, AccountType type) {
            super(accountNumber, ownerId, type);
        }
    }

    static class TestPaymentAccount extends Account {
        public TestPaymentAccount(String accountNumber, String ownerId) {
            super(accountNumber, ownerId, AccountType.PAYMENT);
        }
    }

    static class TestSavingAccount extends Account {
        public TestSavingAccount(String accountNumber, String ownerId) {
            super(accountNumber, ownerId, AccountType.SAVING);
        }
    }

    @Mock
    private IPowerOfAttorneyRepository powerOfAttorneyRepository;
    @Mock
    private IAccountRepository accountRepository;
    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private PowerOfAttorneyService powerOfAttorneyService;

    private String defaultGrantorId;
    private String defaultGranteeId;
    private String defaultAccountNumber;
    private AccountType defaultAccountType;
    private AuthorizationType defaultAuthorizationType;
    private User mockGranteeUser;
    private Account mockAccount;
    private PowerOfAttorney mockPoa;

    @BeforeEach
    void setUp() {
        defaultGrantorId = UUID.randomUUID().toString();
        defaultGranteeId = UUID.randomUUID().toString();
        defaultAccountNumber = "NL12RABO12341234";
        defaultAccountType = AccountType.PAYMENT;
        defaultAuthorizationType = AuthorizationType.READ;
        mockGranteeUser = new User(defaultGranteeId, "Grantee User", "grantee@example.com", "pass");
        mockAccount = new TestPaymentAccount(defaultAccountNumber, defaultGrantorId);
        mockPoa = new PowerOfAttorney(UUID.randomUUID().toString(), defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType);
    }

    @Nested
    @DisplayName("createPowerOfAttorney Tests")
    class CreatePowerOfAttorneyTests {

        @Test
        @DisplayName("should create power of attorney successfully on happy path")
        void createPowerOfAttorney_happyPath_shouldSucceed() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType)).thenReturn(Optional.of(mockAccount));
            when(powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAuthorizationType)).thenReturn(Optional.empty());
            when(powerOfAttorneyRepository.save(any(PowerOfAttorney.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PowerOfAttorney createdPoa = powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType);

            assertNotNull(createdPoa);
            assertThat(createdPoa.getGrantorId()).isEqualTo(defaultGrantorId);
            assertThat(createdPoa.getGranteeId()).isEqualTo(defaultGranteeId);
            assertThat(createdPoa.getAccountNumber()).isEqualTo(defaultAccountNumber);
            assertThat(createdPoa.getAccountType()).isEqualTo(defaultAccountType);
            assertThat(createdPoa.getAuthorizationType()).isEqualTo(defaultAuthorizationType);

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType);
            verify(powerOfAttorneyRepository).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAuthorizationType);
            verify(powerOfAttorneyRepository).save(any(PowerOfAttorney.class));
        }

        @Test
        @DisplayName("should throw InvalidGrantException when grantor and grantee are the same")
        void createPowerOfAttorney_sameGrantorGrantee_shouldThrowException() {
            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGrantorId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(InvalidGrantException.class)
                    .hasMessage("A user cannot grant Power of Attorney to themselves.");

            verifyNoInteractions(userRepository, accountRepository, powerOfAttorneyRepository);
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when grantee is not found")
        void createPowerOfAttorney_granteeNotFound_shouldThrowException() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Grantee not found.");

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository, never()).findByOwnerIdAndAccountNumberAndType(anyString(), anyString(), any());
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(anyString(), anyString(), anyString(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when account is not found or doesn't belong to grantor")
        void createPowerOfAttorney_accountNotFound_shouldThrowException() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Account not found or does not belong to the grantor.");

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(anyString(), anyString(), any());
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(anyString(), anyString(), anyString(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicatePowerOfAttorneyException when an identical grant already exists")
        void createPowerOfAttorney_duplicatePoa_shouldThrowException() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType)).thenReturn(Optional.of(mockAccount));
            when(powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAuthorizationType)).thenReturn(Optional.of(mockPoa));

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(DuplicatePowerOfAttorneyException.class)
                    .hasMessage("An identical Power of Attorney grant already exists for this account and user.");

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(anyString(), anyString(), any());
            verify(powerOfAttorneyRepository).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(anyString(), anyString(), anyString(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should propagate generic RuntimeException when save fails")
        void createPowerOfAttorney_whenSaveFails_shouldPropagateException() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(any(), any(), any())).thenReturn(Optional.of(mockAccount));
            when(powerOfAttorneyRepository.findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any())).thenReturn(Optional.empty());
            when(powerOfAttorneyRepository.save(any(PowerOfAttorney.class))).thenThrow(new RuntimeException("DB down"));

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB down");

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType);
            verify(powerOfAttorneyRepository).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAuthorizationType);
            verify(powerOfAttorneyRepository).save(any(PowerOfAttorney.class));
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when ownerId is null for grantee lookup")
        void createPowerOfAttorney_nullGranteeId_shouldThrowRecordNotFoundException() {
            when(userRepository.findById(isNull())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, null, defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Grantee not found.");

            verify(userRepository).findById(isNull());
            verify(accountRepository, never()).findByOwnerIdAndAccountNumberAndType(any(), any(), any());
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when ownerId is empty for grantee lookup")
        void createPowerOfAttorney_emptyGranteeId_shouldThrowRecordNotFoundException() {
            when(userRepository.findById("")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, "", defaultAccountNumber, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Grantee not found.");

            verify(userRepository).findById("");
            verify(accountRepository, never()).findByOwnerIdAndAccountNumberAndType(any(), any(), any());
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when accountNumber is null for account lookup")
        void createPowerOfAttorney_nullAccountNumber_shouldThrowRecordNotFoundException() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(defaultGrantorId, null, defaultAccountType)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, null, defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Account not found or does not belong to the grantor.");

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, null, defaultAccountType);
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RecordNotFoundException when accountNumber is empty for account lookup")
        void createPowerOfAttorney_emptyAccountNumber_shouldThrowRecordNotFoundException() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(defaultGrantorId, "", defaultAccountType)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, "", defaultAccountType, defaultAuthorizationType))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Account not found or does not belong to the grantor.");

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, "", defaultAccountType);
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when AccountType is null and findByOwnerIdAndAccountNumberAndType doesn't handle it")
        void createPowerOfAttorney_nullAccountType_shouldThrowNPE() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            doThrow(NullPointerException.class).when(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, null);

            assertThrows(NullPointerException.class,
                    () -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, null, defaultAuthorizationType));

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, null);
            verify(powerOfAttorneyRepository, never()).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(any(), any(), any(), any());
            verify(powerOfAttorneyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when AuthorizationType is null and findByGrantorId... doesn't handle it")
        void createPowerOfAttorney_nullAuthorizationType_shouldThrowNPE() {
            when(userRepository.findById(defaultGranteeId)).thenReturn(Optional.of(mockGranteeUser));
            when(accountRepository.findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType)).thenReturn(Optional.of(mockAccount));
            doThrow(NullPointerException.class).when(powerOfAttorneyRepository).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(defaultGrantorId, defaultGranteeId, defaultAccountNumber, null);

            assertThrows(NullPointerException.class,
                    () -> powerOfAttorneyService.createPowerOfAttorney(defaultGrantorId, defaultGranteeId, defaultAccountNumber, defaultAccountType, null));

            verify(userRepository).findById(defaultGranteeId);
            verify(accountRepository).findByOwnerIdAndAccountNumberAndType(defaultGrantorId, defaultAccountNumber, defaultAccountType);
            verify(powerOfAttorneyRepository).findByGrantorIdAndGranteeIdAndAccountNumberAndAuthorizationType(defaultGrantorId, defaultGranteeId, defaultAccountNumber, null);
            verify(powerOfAttorneyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAccessGrantsForGrantee Tests")
    class GetAccessGrantsForGranteeTests {

        @Test
        @DisplayName("should return all grants when authorization type is not specified")
        void getAccessGrants_whenAuthTypeIsEmpty_shouldReturnAllGrantsForGrantee() {
            List<PowerOfAttorney> grants = List.of(mockPoa, new PowerOfAttorney(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultGranteeId, "NL98RABO11112222", AccountType.SAVING, AuthorizationType.READ));
            when(powerOfAttorneyRepository.findByGranteeId(defaultGranteeId)).thenReturn(grants);

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(defaultGranteeId, Optional.empty());

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getGranteeId()).isEqualTo(defaultGranteeId);
            assertThat(results.get(1).getGranteeId()).isEqualTo(defaultGranteeId);
            verify(powerOfAttorneyRepository).findByGranteeId(defaultGranteeId);
            verify(powerOfAttorneyRepository, never()).findByGranteeIdAndAuthorizationType(anyString(), any());
        }

        @Test
        @DisplayName("should return filtered grants when authorization type is specified")
        void getAccessGrants_whenAuthTypeIsSpecified_shouldReturnFilteredGrants() {
            List<PowerOfAttorney> grants = List.of(mockPoa);
            when(powerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(defaultGranteeId, defaultAuthorizationType)).thenReturn(grants);

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(defaultGranteeId, Optional.of(defaultAuthorizationType));

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getGranteeId()).isEqualTo(defaultGranteeId);
            assertThat(results.get(0).getAuthorizationType()).isEqualTo(defaultAuthorizationType);
            verify(powerOfAttorneyRepository, never()).findByGranteeId(anyString());
            verify(powerOfAttorneyRepository).findByGranteeIdAndAuthorizationType(defaultGranteeId, defaultAuthorizationType);
        }

        @Test
        @DisplayName("should return an empty list when no grants are found")
        void getAccessGrants_whenNoGrantsFound_shouldReturnEmptyList() {
            when(powerOfAttorneyRepository.findByGranteeId(defaultGranteeId)).thenReturn(Collections.emptyList());

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(defaultGranteeId, Optional.empty());

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when no grants are found for grantee and auth type")
        void getAccessGrants_noGrantsFoundWithAuthType_shouldReturnEmptyList() {
            when(powerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(defaultGranteeId, defaultAuthorizationType)).thenReturn(Collections.emptyList());

            List<PowerOfAttorney> results = powerOfAttorneyService.getAccessGrantsForGrantee(defaultGranteeId, Optional.of(defaultAuthorizationType));

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should propagate exception when repository fails for getAccessGrantsForGrantee")
        void getAccessGrants_whenRepositoryFails_shouldPropagateException() {
            when(powerOfAttorneyRepository.findByGranteeId(defaultGranteeId)).thenThrow(new RuntimeException("Database error during fetch"));

            assertThatThrownBy(() -> powerOfAttorneyService.getAccessGrantsForGrantee(defaultGranteeId, Optional.empty()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error during fetch");

            verify(powerOfAttorneyRepository).findByGranteeId(defaultGranteeId);
        }

        @Test
        @DisplayName("should propagate exception when repository fails for getAccessGrantsForGrantee with auth type")
        void getAccessGrants_whenRepositoryFailsWithAuthType_shouldPropagateException() {
            when(powerOfAttorneyRepository.findByGranteeIdAndAuthorizationType(defaultGranteeId, defaultAuthorizationType)).thenThrow(new RuntimeException("Database error during filtered fetch"));

            assertThatThrownBy(() -> powerOfAttorneyService.getAccessGrantsForGrantee(defaultGranteeId, Optional.of(defaultAuthorizationType)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error during filtered fetch");

            verify(powerOfAttorneyRepository).findByGranteeIdAndAuthorizationType(defaultGranteeId, defaultAuthorizationType);
        }

        @Test
        @DisplayName("should handle null granteeId gracefully for getAccessGrantsForGrantee")
        void getAccessGrantsForGrantee_nullGranteeId_shouldThrowException() {
            when(powerOfAttorneyRepository.findByGranteeId(isNull())).thenThrow(new IllegalArgumentException("Grantee ID cannot be null"));

            assertThatThrownBy(() -> powerOfAttorneyService.getAccessGrantsForGrantee(null, Optional.empty()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Grantee ID cannot be null");

            verify(powerOfAttorneyRepository).findByGranteeId(isNull());
        }

        @Test
        @DisplayName("should handle empty granteeId gracefully for getAccessGrantsForGrantee")
        void getAccessGrantsForGrantee_emptyGranteeId_shouldReturnEmptyList() {
            when(powerOfAttorneyRepository.findByGranteeId("")).thenReturn(Collections.emptyList());

            List<PowerOfAttorney> result = powerOfAttorneyService.getAccessGrantsForGrantee("", Optional.empty());

            assertThat(result).isEmpty();
            verify(powerOfAttorneyRepository).findByGranteeId("");
        }
    }
}