package nl.rabobank;

import nl.rabobank.dto.CustomUserDetails;
import nl.rabobank.exception.UserAlreadyExistsException;
import nl.rabobank.mongo.document.user.UserDocument;
import nl.rabobank.mongo.repository.UserRepository;
import nl.rabobank.service.AuthenticationService;
import nl.rabobank.service.JwtService;
import nl.rabobank.user.User;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserDocument userDocument;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDocument = new UserDocument();
        userDocument.setId("user-id-123");
        userDocument.setEmail("test@rabobank.nl");
        userDocument.setName("Test User");
        userDocument.setPassword("encodedPassword123");
        userDetails = new CustomUserDetails(userDocument);
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register should succeed for a new user")
        void register_whenUserIsNew_shouldSucceed() {
            when(userRepository.findByEmail("test@rabobank.nl")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword123");
            when(modelMapper.map(any(User.class), eq(UserDocument.class))).thenReturn(userDocument);
            when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token-string");

            String token = authenticationService.register("test@rabobank.nl", "Test User", "rawPassword");

            assertThat(token).isEqualTo("jwt-token-string");
            ArgumentCaptor<UserDocument> userDocumentCaptor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository).save(userDocumentCaptor.capture());
            UserDocument savedUser = userDocumentCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("test@rabobank.nl");
            assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123");
            verify(jwtService).generateToken(any(CustomUserDetails.class));
        }

        @Test
        @DisplayName("register should throw UserAlreadyExistsException if email is taken")
        void register_whenEmailExists_shouldThrowException() {
            when(userRepository.findByEmail("test@rabobank.nl")).thenReturn(Optional.of(userDocument));

            assertThatThrownBy(() -> authenticationService.register("test@rabobank.nl", "Test User", "rawPassword"))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessage("Email is already in use.");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("register should succeed even with an empty name")
        void register_withEmptyName_shouldSucceed() {
            UserDocument userDocWithEmptyName = new UserDocument();
            userDocWithEmptyName.setName("");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(modelMapper.map(any(User.class), eq(UserDocument.class))).thenReturn(userDocWithEmptyName);
            when(jwtService.generateToken(any())).thenReturn("token");

            authenticationService.register("test@rabobank.nl", "", "password");

            ArgumentCaptor<UserDocument> captor = ArgumentCaptor.forClass(UserDocument.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("");
        }

        @Test
        @DisplayName("register should propagate exception if password encoder fails on null password")
        void register_withNullPassword_shouldPropagateException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(null)).thenThrow(new IllegalArgumentException("Raw password cannot be null"));

            assertThatThrownBy(() -> authenticationService.register("test@rabobank.nl", "Test User", null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("register should propagate exception if repository save fails")
        void register_whenSaveFails_shouldPropagateException() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(modelMapper.map(any(), any())).thenReturn(userDocument);
            when(userRepository.save(any(UserDocument.class))).thenThrow(new DataIntegrityViolationException("Save failed"));

            assertThatThrownBy(() -> authenticationService.register("test@rabobank.nl", "Test User", "rawPassword"))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("register should return null if JWT service returns null")
        void register_whenJwtServiceReturnsNull_shouldReturnNull() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(modelMapper.map(any(), any())).thenReturn(userDocument);
            when(userRepository.save(any())).thenReturn(userDocument);
            when(jwtService.generateToken(any())).thenReturn(null);

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
            when(userDetailsService.loadUserByUsername("test@rabobank.nl")).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn("jwt-token-string");

            String token = authenticationService.login("test@rabobank.nl", "correctPassword");

            assertThat(token).isEqualTo("jwt-token-string");

            ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(authTokenCaptor.capture());

            assertThat(authTokenCaptor.getValue().getName()).isEqualTo("test@rabobank.nl");
            assertThat(authTokenCaptor.getValue().getCredentials()).isEqualTo("correctPassword");

            verify(userDetailsService).loadUserByUsername("test@rabobank.nl");
            verify(jwtService).generateToken(userDetails);
        }

        @Test
        @DisplayName("login should throw BadCredentialsException for incorrect password")
        void login_withIncorrectPassword_shouldThrowException() {
            doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authenticationService.login("test@rabobank.nl", "wrongPassword"))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userDetailsService, never()).loadUserByUsername(anyString());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("login should throw exception when called with null email")
        void login_withNullEmail_shouldThrowException() {
            doThrow(new BadCredentialsException("Email cannot be null.")).when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authenticationService.login(null, "password"))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("login should throw exception when called with null password")
        void login_withNullPassword_shouldThrowException() {
            doThrow(new BadCredentialsException("Null password")).when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authenticationService.login("test@rabobank.nl", null))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("login should propagate DisabledException if user is disabled")
        void login_whenUserIsDisabled_shouldThrowException() {
            doThrow(new DisabledException("User is disabled")).when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authenticationService.login("disabled@rabobank.nl", "password"))
                    .isInstanceOf(DisabledException.class);
        }

        @Test
        @DisplayName("login should throw NullPointerException if user details service returns null")
        void login_whenUserDetailsServiceReturnsNull_shouldThrowNPE() {
            when(userDetailsService.loadUserByUsername("test@rabobank.nl")).thenReturn(null);

            when(jwtService.generateToken(null)).thenThrow(new NullPointerException());

            assertThatThrownBy(() -> authenticationService.login("test@rabobank.nl", "password"))
                    .isInstanceOf(NullPointerException.class);

            verify(jwtService).generateToken(null);
        }
    }
}