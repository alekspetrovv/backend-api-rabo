package nl.rabobank;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import nl.rabobank.dto.CustomUserDetails;
import nl.rabobank.security.JwtService;
import nl.rabobank.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private final String testSecretKey = Base64.getEncoder().encodeToString("a-simple-but-secure-secret-key-for-unit-testing-hs256".getBytes());
    private final long testExpirationInMillis = TimeUnit.MINUTES.toMillis(30);

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpirationInMillis);

        User user = new User();
        user.setEmail("test@rabobank.nl");
        userDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("Should extract the correct username from a generated token")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Should validate a correctly generated token successfully")
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should fail validation for a token belonging to a different user")
    void isTokenValid_withDifferentUser_shouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);

        User otherUser = new User();
        otherUser.setEmail("other-user@rabobank.nl");
        UserDetails otherUserDetails = new CustomUserDetails(otherUser);

        boolean isValid = jwtService.isTokenValid(token, otherUserDetails);
        assertThat(isValid).isFalse();
    }


    @Test
    @DisplayName("Should throw exception for a malformed token string")
    void isTokenValid_withMalformedToken_shouldThrowException() {
        String malformedToken = "this-is-not-a-valid-jwt";
        assertThatThrownBy(() -> jwtService.isTokenValid(malformedToken, userDetails))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Should throw SignatureException for a token signed with a different key")
    void isTokenValid_withDifferentSignatureKey_shouldThrowException() {
        String token = jwtService.generateToken(userDetails);

        JwtService anotherJwtService = new JwtService();
        String anotherSecretKey = Base64.getEncoder().encodeToString("a-completely-different-secret-key-that-does-not-match".getBytes());
        ReflectionTestUtils.setField(anotherJwtService, "secretKey", anotherSecretKey);
        ReflectionTestUtils.setField(anotherJwtService, "jwtExpiration", testExpirationInMillis);

        assertThatThrownBy(() -> anotherJwtService.isTokenValid(token, userDetails))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should generate a token containing extra claims when provided")
    void generateToken_withExtraClaims_shouldContainExtraClaims() {
        Map<String, Object> extraClaims = Map.of("userId", "user-id-123", "role", "ADMIN");
        String token = jwtService.generateToken(extraClaims, userDetails);

        Claims claims = (Claims) ReflectionTestUtils.invokeMethod(jwtService, "extractAllClaims", token);

        assertThat(claims.getSubject()).isEqualTo("test@rabobank.nl");
        assertThat(claims.get("userId", String.class)).isEqualTo("user-id-123");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }
}