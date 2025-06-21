package nl.rabobank;

import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.config.AccountFactory;
import nl.rabobank.mongo.document.account.AccountDocument;
import nl.rabobank.mongo.repository.AccountRepository;
import nl.rabobank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.MappingException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceUnitTests {

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
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AccountFactory accountFactory;

    @InjectMocks
    private AccountService accountService;

    private Account mockPaymentAccount;
    private AccountDocument mockAccountDocument;
    private String defaultOwnerId = "user-123";
    private String defaultAccountNumber = "NL01RABO0123456789";

    @BeforeEach
    void setUp() {
        mockPaymentAccount = new TestPaymentAccount(defaultAccountNumber, defaultOwnerId);
        mockAccountDocument = new AccountDocument();
        mockAccountDocument.setAccountNumber(defaultAccountNumber);
        mockAccountDocument.setOwnerId(defaultOwnerId);
        mockAccountDocument.setType(AccountType.PAYMENT);
        mockAccountDocument.setBalance(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createAccount should succeed on happy path for PAYMENT account")
    void createAccount_happyPathForPaymentAccount_shouldSucceed() {
        when(accountFactory.createAccount(eq(AccountType.PAYMENT), eq(defaultOwnerId))).thenReturn(mockPaymentAccount);
        when(modelMapper.map(mockPaymentAccount, AccountDocument.class)).thenReturn(mockAccountDocument);
        when(accountRepository.save(mockAccountDocument)).thenReturn(mockAccountDocument);

        Account result = accountService.createAccount(AccountType.PAYMENT, defaultOwnerId);

        assertNotNull(result);
        assertEquals(defaultOwnerId, result.getOwnerId());
        ArgumentCaptor<AccountDocument> documentCaptor = ArgumentCaptor.forClass(AccountDocument.class);
        verify(accountRepository).save(documentCaptor.capture());
        assertEquals(defaultAccountNumber, documentCaptor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("createAccount should succeed on happy path for SAVING account")
    void createAccount_happyPathForSavingAccount_shouldSucceed() {
        String savingOwnerId = "user-456";
        Account mockSavingAccount = new TestSavingAccount("NL02RABO0987654321", savingOwnerId);
        mockAccountDocument.setType(AccountType.SAVING);

        when(accountFactory.createAccount(eq(AccountType.SAVING), eq(savingOwnerId))).thenReturn(mockSavingAccount);
        when(modelMapper.map(mockSavingAccount, AccountDocument.class)).thenReturn(mockAccountDocument);
        when(accountRepository.save(mockAccountDocument)).thenReturn(mockAccountDocument);

        Account result = accountService.createAccount(AccountType.SAVING, savingOwnerId);

        assertNotNull(result);
        assertEquals(savingOwnerId, result.getOwnerId());
        assertEquals(AccountType.SAVING, result.getType());
        verify(accountRepository).save(any(AccountDocument.class));
    }

    @Test
    @DisplayName("createAccount should call dependencies in the correct order")
    void createAccount_shouldCallDependenciesInOrder() {
        when(accountFactory.createAccount(any(), any())).thenReturn(mockPaymentAccount);
        when(modelMapper.map(any(), any())).thenReturn(mockAccountDocument);

        accountService.createAccount(AccountType.PAYMENT, defaultOwnerId);

        InOrder inOrder = inOrder(accountFactory, modelMapper, accountRepository);

        inOrder.verify(accountFactory).createAccount(AccountType.PAYMENT, defaultOwnerId);
        inOrder.verify(modelMapper).map(mockPaymentAccount, AccountDocument.class);
        inOrder.verify(accountRepository).save(mockAccountDocument);
    }

    @Test
    @DisplayName("createAccount should propagate DataAccessResourceFailureException when repository fails to connect")
    void createAccount_whenRepositoryThrowsDataAccessException_shouldPropagateException() {
        when(accountFactory.createAccount(any(), any())).thenReturn(mockPaymentAccount);
        when(modelMapper.map(any(), any())).thenReturn(mockAccountDocument);
        when(accountRepository.save(any(AccountDocument.class))).thenThrow(new DataAccessResourceFailureException("Database is down"));

        assertThrows(DataAccessResourceFailureException.class, () -> accountService.createAccount(AccountType.PAYMENT, defaultOwnerId));
    }

    @Test
    @DisplayName("createAccount should propagate DuplicateKeyException when account already exists")
    void createAccount_whenRepositoryThrowsDuplicateKeyException_shouldPropagateException() {
        when(accountFactory.createAccount(any(), any())).thenReturn(mockPaymentAccount);
        when(modelMapper.map(any(), any())).thenReturn(mockAccountDocument);
        when(accountRepository.save(any(AccountDocument.class))).thenThrow(new DuplicateKeyException("Account with this number already exists"));

        assertThrows(DuplicateKeyException.class, () -> accountService.createAccount(AccountType.PAYMENT, defaultOwnerId));
    }

    @Test
    @DisplayName("createAccount should propagate exception when factory throws an exception")
    void createAccount_whenFactoryThrowsException_shouldPropagate() {
        when(accountFactory.createAccount(any(), any())).thenThrow(new IllegalArgumentException("Invalid owner ID"));

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(AccountType.PAYMENT, ""));
    }

    @Test
    @DisplayName("createAccount should propagate exception when mapper throws an exception")
    void createAccount_whenMapperThrowsException_shouldPropagate() {
        when(accountFactory.createAccount(any(), any())).thenReturn(mockPaymentAccount);
        when(modelMapper.map(any(Account.class), eq(AccountDocument.class))).thenThrow(new MappingException(Collections.emptyList()));

        assertThrows(MappingException.class, () -> accountService.createAccount(AccountType.PAYMENT, defaultOwnerId));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAccount should call save with null if mapper returns null")
    void createAccount_whenMapperReturnsNull_shouldCallSaveWithNull() {
        when(accountFactory.createAccount(any(), any())).thenReturn(mockPaymentAccount);
        when(modelMapper.map(any(Account.class), eq(AccountDocument.class))).thenReturn(null);

        accountService.createAccount(AccountType.PAYMENT, defaultOwnerId);

        verify(accountRepository).save(isNull());
    }

    @Test
    @DisplayName("createAccount should return the original object from factory, not the one from the repository")
    void createAccount_shouldReturnObjectFromFactory() {
        Account factoryAccount = new TestPaymentAccount(defaultAccountNumber, defaultOwnerId);
        AccountDocument savedDocument = new AccountDocument();
        savedDocument.setCreatedAt(Instant.now().plusSeconds(10));

        when(accountFactory.createAccount(any(), any())).thenReturn(factoryAccount);
        when(modelMapper.map(any(), any())).thenReturn(mockAccountDocument);
        when(accountRepository.save(any())).thenReturn(savedDocument);

        Account result = accountService.createAccount(AccountType.PAYMENT, defaultOwnerId);

        assertSame(factoryAccount, result, "The returned object should be the same instance from the factory");
        assertNotEquals(savedDocument.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    @DisplayName("createAccount should handle null ownerId input gracefully if factory allows it")
    void createAccount_withNullOwnerId_shouldProceedIfFactoryAllows() {
        Account accountWithNullOwner = new TestPaymentAccount(defaultAccountNumber, null);
        when(accountFactory.createAccount(eq(AccountType.PAYMENT), isNull())).thenReturn(accountWithNullOwner);
        when(modelMapper.map(any(), any())).thenReturn(mockAccountDocument);
        when(accountRepository.save(any())).thenReturn(mockAccountDocument);

        Account result = accountService.createAccount(AccountType.PAYMENT, null);

        assertNotNull(result);
        assertNull(result.getOwnerId());
        verify(accountFactory).createAccount(AccountType.PAYMENT, null);
        verify(accountRepository).save(any(AccountDocument.class));
    }
}