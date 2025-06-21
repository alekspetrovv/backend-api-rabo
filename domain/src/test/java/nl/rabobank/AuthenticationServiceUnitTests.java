package nl.rabobank;

import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.exception.UserAlreadyExistsException;
import nl.rabobank.user.User;
import nl.rabobank.user.IUserRepository;
import nl.rabobank.user.IPasswordHasher;
import nl.rabobank.user.ITokenGenerator;
import nl.rabobank.user.IAuthenticationPort;
import nl.rabobank.user.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Pure Domain Unit Tests")
class AuthenticationServiceUnitTests {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private IPasswordHasher passwordHasher;
    @Mock
    private ITokenGenerator tokenGenerator;
    @Mock
    private IAuthenticationPort authenticationPort;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User domainUser;

    @BeforeEach
    void setUp() {
        domainUser = new User("user-id-123", "Test User", "test@rabobank.nl", "encodedPassword123");
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register should succeed for a new user")
        void register_whenUserIsNew_shouldSucceed() {
            when(userRepository.findByEmail("test@rabobank.nl")).thenReturn(Optional.empty());
            when(passwordHasher.hashPassword("rawPassword")).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(domainUser);
            when(tokenGenerator.generateTokenForUser(any(User.class))).thenReturn("jwt-token-string");

            String token = authenticationService.register("test@rabobank.nl", "Test User", "rawPassword");

            assertThat(token).isEqualTo("jwt-token-string");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("test@rabobank.nl");
            assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123");
            verify(tokenGenerator).generateTokenForUser(savedUser);
        }

        @Test
        @DisplayName("register should throw UserAlreadyExistsException if email is taken")
        void register_whenEmailExists_shouldThrowException() {
            when(userRepository.findByEmail("test@rabobank.nl")).thenReturn(Optional.of(domainUser));

            assertThatThrownBy(() -> authenticationService.register("test@rabobank.nl", "Test User", "rawPassword"))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessage("Email is already in use.");

            verify(passwordHasher, never()).hashPassword(anyString());
            verify(userRepository, never()).save(any());
            verify(tokenGenerator, never()).generateTokenForUser(any());
        }

        @Test
        @DisplayName("register should succeed even with an empty name")
        void register_withEmptyName_shouldSucceed() {
            User userWithEmptyName = new User("some-id", "", "test@rabobank.nl", "encoded");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordHasher.hashPassword(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(userWithEmptyName);
            when(tokenGenerator.generateTokenForUser(any(User.class))).thenReturn("token");

            authenticationService.register("test@rabobank.nl", "", "password");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("");
        }

        @Test
        @DisplayName("register should propagate exception if password hasher fails on null password")
        void register_withNullPassword_shouldPropagateException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordHasher.hashPassword(null)).thenThrow(new IllegalArgumentException("Raw password cannot be null"));

            assertThatThrownBy(() -> authenticationService.register("test@rabobank.nl", "Test User", null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("register should propagate exception if repository save fails")
        void register_whenSaveFails_shouldPropagateException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordHasher.hashPassword(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database save failed unexpectedly"));

            assertThatThrownBy(() -> authenticationService.register("test@rabobank.nl", "Test User", "rawPassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database save failed unexpectedly");

            verify(tokenGenerator, never()).generateTokenForUser(any());
        }

        @Test
        @DisplayName("register should return null if token generator returns null")
        void register_whenTokenGeneratorReturnsNull_shouldReturnNull() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordHasher.hashPassword(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(domainUser);
            when(tokenGenerator.generateTokenForUser(any(User.class))).thenReturn(null);

            String token = authenticationService.register("test@rabobank.nl", "Test User", "password");

            assertThat(token).isNull();
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("login should succeed with correct credentials")
        void login_withCorrectCredentials_shouldReturnJwtToken() {
            doNothing().when(authenticationPort).authenticate("test@rabobank.nl", "correctPassword");
            when(userRepository.findByEmail("test@rabobank.nl")).thenReturn(Optional.of(domainUser));
            when(tokenGenerator.generateTokenForUser(domainUser)).thenReturn("jwt-token-string");

            String token = authenticationService.login("test@rabobank.nl", "correctPassword");

            assertThat(token).isEqualTo("jwt-token-string");

            verify(authenticationPort).authenticate("test@rabobank.nl", "correctPassword");
            verify(userRepository).findByEmail("test@rabobank.nl");
            verify(tokenGenerator).generateTokenForUser(domainUser);
        }

        @Test
        @DisplayName("login should throw exception for incorrect password")
        void login_withIncorrectPassword_shouldThrowException() {
            doThrow(new RuntimeException("Authentication failed: Bad credentials")).when(authenticationPort).authenticate(anyString(), anyString());

            assertThatThrownBy(() -> authenticationService.login("test@rabobank.nl", "wrongPassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Authentication failed: Bad credentials");

            verify(userRepository, never()).findByEmail(anyString());
            verify(tokenGenerator, never()).generateTokenForUser(any());
        }

        @Test
        @DisplayName("login should throw exception when called with null email")
        void login_withNullEmail_shouldThrowException() {
            doThrow(new IllegalArgumentException("Username cannot be null")).when(authenticationPort).authenticate(isNull(), anyString());

            assertThatThrownBy(() -> authenticationService.login(null, "password"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username cannot be null");
        }

        @Test
        @DisplayName("login should throw exception when called with null password")
        void login_withNullPassword_shouldThrowException() {
            doThrow(new IllegalArgumentException("Password cannot be null")).when(authenticationPort).authenticate(anyString(), isNull());

            assertThatThrownBy(() -> authenticationService.login("test@rabobank.nl", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be null");
        }

        @Test
        @DisplayName("login should throw exception if user is disabled or inactive")
        void login_whenUserIsDisabled_shouldThrowException() {
            doThrow(new RuntimeException("Authentication failed: User is disabled")).when(authenticationPort).authenticate(anyString(), anyString());

            assertThatThrownBy(() -> authenticationService.login("disabled@rabobank.nl", "password"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Authentication failed: User is disabled");
        }

        @Test
        @DisplayName("login should throw RecordNotFoundException if authenticated user not found in repository")
        void login_whenAuthenticatedUserNotFound_shouldThrowRecordNotFoundException() {
            doNothing().when(authenticationPort).authenticate("test@rabobank.nl", "correctPassword");
            when(userRepository.findByEmail("test@rabobank.nl")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.login("test@rabobank.nl", "correctPassword"))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessageStartingWith("User with email not found:");
        }
    }
}