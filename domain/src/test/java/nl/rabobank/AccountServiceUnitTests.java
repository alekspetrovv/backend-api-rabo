package nl.rabobank;

import nl.rabobank.account.*;
import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.user.IUserRepository;
import nl.rabobank.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceUnitTests {

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
    private IAccountRepository accountRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private AccountFactory accountFactory;

    @InjectMocks
    private AccountService accountService;

    private String defaultOwnerId;
    private String defaultAccountNumber;
    private Account mockPaymentAccount;
    private Account mockSavingAccount;
    private User mockOwnerUser;

    @BeforeEach
    void setUp() {
        defaultOwnerId = UUID.randomUUID().toString();
        defaultAccountNumber = "NL" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        mockPaymentAccount = new TestPaymentAccount(defaultAccountNumber, defaultOwnerId);
        mockSavingAccount = new TestSavingAccount(defaultAccountNumber, defaultOwnerId);

        mockOwnerUser = new User(defaultOwnerId, "Test User", "test@example.com", "hashedpass");
    }

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPathTests {

        @Test
        @DisplayName("createAccount should successfully create a PAYMENT account")
        void createAccount_paymentAccount_shouldSucceed() {
            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.of(mockOwnerUser));
            when(accountFactory.createAccount(AccountType.PAYMENT, defaultOwnerId)).thenReturn(mockPaymentAccount);
            when(accountRepository.save(mockPaymentAccount)).thenReturn(mockPaymentAccount);

            Account result = accountService.createAccount(AccountType.PAYMENT, defaultOwnerId);

            assertNotNull(result);
            assertEquals(defaultAccountNumber, result.getAccountNumber());
            assertEquals(defaultOwnerId, result.getOwnerId());
            assertEquals(AccountType.PAYMENT, result.getType());
            assertEquals(BigDecimal.ZERO, result.getBalance());

            verify(userRepository).findById(defaultOwnerId);
            verify(accountFactory).createAccount(AccountType.PAYMENT, defaultOwnerId);
            verify(accountRepository).save(mockPaymentAccount);
        }

        @Test
        @DisplayName("createAccount should successfully create a SAVING account")
        void createAccount_savingAccount_shouldSucceed() {
            String savingOwnerId = UUID.randomUUID().toString();
            String savingAccountNumber = "NL" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            Account mockSavingAccountSpecific = new TestSavingAccount(savingAccountNumber, savingOwnerId);
            User mockSavingOwnerUser = new User(savingOwnerId, "Saving User", "saving@example.com", "hashedpass");

            when(userRepository.findById(savingOwnerId)).thenReturn(Optional.of(mockSavingOwnerUser));
            when(accountFactory.createAccount(AccountType.SAVING, savingOwnerId)).thenReturn(mockSavingAccountSpecific);
            when(accountRepository.save(mockSavingAccountSpecific)).thenReturn(mockSavingAccountSpecific);

            Account result = accountService.createAccount(AccountType.SAVING, savingOwnerId);

            assertNotNull(result);
            assertEquals(savingAccountNumber, result.getAccountNumber());
            assertEquals(savingOwnerId, result.getOwnerId());
            assertEquals(AccountType.SAVING, result.getType());
            assertEquals(BigDecimal.ZERO, result.getBalance());

            verify(userRepository).findById(savingOwnerId);
            verify(accountFactory).createAccount(AccountType.SAVING, savingOwnerId);
            verify(accountRepository).save(mockSavingAccountSpecific);
        }

        @Test
        @DisplayName("createAccount should call dependencies in correct order: userRepository -> accountFactory -> accountRepository")
        void createAccount_shouldCallDependenciesInOrder() {
            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.of(mockOwnerUser));
            when(accountFactory.createAccount(any(AccountType.class), anyString())).thenReturn(mockPaymentAccount);
            when(accountRepository.save(any(Account.class))).thenReturn(mockPaymentAccount);

            InOrder inOrder = inOrder(userRepository, accountFactory, accountRepository);

            accountService.createAccount(AccountType.PAYMENT, defaultOwnerId);

            inOrder.verify(userRepository).findById(defaultOwnerId);
            inOrder.verify(accountFactory).createAccount(AccountType.PAYMENT, defaultOwnerId);
            inOrder.verify(accountRepository).save(mockPaymentAccount);
        }

        @Test
        @DisplayName("createAccount should return the exact Account object returned by the factory")
        void createAccount_shouldReturnFactoryObject() {
            Account factoryCreatedAccount = new TestPaymentAccount("NL12345", "ownerX");
            User factoryOwnerUser = new User("ownerX", "Factory User", "factory@example.com", "pass");

            when(userRepository.findById("ownerX")).thenReturn(Optional.of(factoryOwnerUser));
            when(accountFactory.createAccount(any(AccountType.class), anyString())).thenReturn(factoryCreatedAccount);
            when(accountRepository.save(factoryCreatedAccount)).thenReturn(factoryCreatedAccount);

            Account result = accountService.createAccount(AccountType.PAYMENT, "ownerX");

            assertSame(factoryCreatedAccount, result);
        }
    }

    @Nested
    @DisplayName("Edge Cases & Error Handling")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("createAccount should throw RecordNotFoundException if ownerId not found")
        void createAccount_whenOwnerIdNotFound_shouldThrowRecordNotFoundException() {
            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.createAccount(AccountType.PAYMENT, defaultOwnerId))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Owner with ID " + defaultOwnerId + " not found.");

            verify(userRepository).findById(defaultOwnerId);
            verify(accountFactory, never()).createAccount(any(), any());
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("createAccount should propagate IllegalArgumentException from AccountFactory")
        void createAccount_whenFactoryThrowsIllegalArgumentException_shouldPropagate() {
            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.of(mockOwnerUser));
            doThrow(new IllegalArgumentException("Invalid input for account factory")).when(accountFactory)
                    .createAccount(any(AccountType.class), anyString());

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createAccount(AccountType.PAYMENT, defaultOwnerId));

            verify(userRepository).findById(defaultOwnerId);
            verify(accountFactory).createAccount(AccountType.PAYMENT, defaultOwnerId);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("createAccount should propagate generic RuntimeException from AccountFactory")
        void createAccount_whenFactoryThrowsRuntimeException_shouldPropagate() {
            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.of(mockOwnerUser));
            doThrow(new RuntimeException("Factory unexpected error")).when(accountFactory)
                    .createAccount(any(AccountType.class), anyString());

            assertThrows(RuntimeException.class,
                    () -> accountService.createAccount(AccountType.SAVING, defaultOwnerId));

            verify(userRepository).findById(defaultOwnerId);
            verify(accountFactory).createAccount(AccountType.SAVING, defaultOwnerId);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("createAccount should propagate RuntimeException from IAccountRepository.save")
        void createAccount_whenRepositorySaveThrowsRuntimeException_shouldPropagate() {
            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.of(mockOwnerUser));
            when(accountFactory.createAccount(any(AccountType.class), anyString())).thenReturn(mockPaymentAccount);
            doThrow(new RuntimeException("Persistence error")).when(accountRepository).save(any(Account.class));

            assertThrows(RuntimeException.class,
                    () -> accountService.createAccount(AccountType.PAYMENT, defaultOwnerId));

            verify(userRepository).findById(defaultOwnerId);
            verify(accountFactory).createAccount(AccountType.PAYMENT, defaultOwnerId);
            verify(accountRepository).save(mockPaymentAccount);
        }

        @Test
        @DisplayName("createAccount should handle null AccountType gracefully if factory allows it")
        void createAccount_withNullAccountType_shouldProceedIfFactoryAllows() {
            User testUser = new User(defaultOwnerId, "Test User", "test@nulltype.com", "pass");
            Account accountWithNullType = new TestAccountWithNullableType("NL999", defaultOwnerId, null);

            when(userRepository.findById(defaultOwnerId)).thenReturn(Optional.of(testUser));
            when(accountFactory.createAccount(isNull(), eq(defaultOwnerId))).thenReturn(accountWithNullType);
            when(accountRepository.save(accountWithNullType)).thenReturn(accountWithNullType);

            Account result = accountService.createAccount(null, defaultOwnerId);

            assertNotNull(result);
            assertNull(result.getType());
            verify(userRepository).findById(defaultOwnerId);
            verify(accountFactory).createAccount(null, defaultOwnerId);
            verify(accountRepository).save(accountWithNullType);
        }

        @Test
        @DisplayName("createAccount should handle null ownerId in input if userRepository handles it (before validation)")
        void createAccount_withNullOwnerId_shouldThrowRecordNotFoundIfUserRepoHandlesNull() {
            when(userRepository.findById(isNull())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.createAccount(AccountType.PAYMENT, null))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Owner with ID null not found.");
            verify(userRepository).findById(isNull());
            verify(accountFactory, never()).createAccount(any(), any());
            verify(accountRepository, never()).save(any());
        }
    }
}