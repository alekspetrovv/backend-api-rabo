package nl.rabobank;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.attorney.PowerOfAttorney;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("PowerOfAttorney Domain Class Tests")
class PowerOfAttorneyTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation for a valid power of attorney")
    void validate_withValidPowerOfAttorney_shouldHaveNoViolations() {
        PowerOfAttorney poa = new PowerOfAttorney(
                "poa-id-1", "grantor-1", "grantee-1", "NL01RABO0123456789",
                AccountType.PAYMENT, AuthorizationType.READ
        );
        Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Constructors and setter should function correctly")
    void constructorsAndSetter_shouldFunctionCorrectly() {
        PowerOfAttorney poaWithAutoId = new PowerOfAttorney(
                "grantor-1", "grantee-1", "NL01RABO0123456789",
                AccountType.PAYMENT, AuthorizationType.READ
        );
        assertThat(poaWithAutoId.getId()).isNotNull();
        assertThatCode(() -> UUID.fromString(poaWithAutoId.getId())).doesNotThrowAnyException();
        assertThat(poaWithAutoId.getGrantorId()).isEqualTo("grantor-1");

        poaWithAutoId.setAuthorizationType(AuthorizationType.WRITE);
        assertThat(poaWithAutoId.getAuthorizationType()).isEqualTo(AuthorizationType.WRITE);
    }

    @Nested
    @DisplayName("ID and Grantee/Grantor Validation")
    class IdValidation {
        @Test
        @DisplayName("Should fail validation when grantorId is blank")
        void validate_withBlankGrantorId_shouldFail() {
            PowerOfAttorney poa = new PowerOfAttorney(
                    "poa-id-1", " ", "grantee-1", "NL01RABO0123456789",
                    AccountType.PAYMENT, AuthorizationType.READ
            );
            Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Grantor ID cannot be null or empty.");
        }

        @Test
        @DisplayName("Should fail validation when granteeId is blank")
        void validate_withBlankGranteeId_shouldFail() {
            PowerOfAttorney poa = new PowerOfAttorney(
                    "poa-id-1", "grantor-1", " ", "NL01RABO0123456789",
                    AccountType.PAYMENT, AuthorizationType.READ
            );
            Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Grantee ID cannot be null or empty.");
        }
    }

    @Nested
    @DisplayName("Account Number Validation")
    class AccountNumberValidation {
        @Test
        @DisplayName("Should fail validation for a malformed account number")
        void validate_withMalformedAccountNumber_shouldFail() {
            PowerOfAttorney poa = new PowerOfAttorney(
                    "poa-id-1", "grantor-1", "grantee-1", "INVALID-IBAN",
                    AccountType.PAYMENT, AuthorizationType.READ
            );
            Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Account number must be a valid format.");
        }
    }

    @Nested
    @DisplayName("Type Validation")
    class TypeValidation {
        @Test
        @DisplayName("Should fail validation when accountType is null")
        void validate_withNullAccountType_shouldFail() {
            PowerOfAttorney poa = new PowerOfAttorney(
                    "poa-id-1", "grantor-1", "grantee-1", "NL01RABO0123456789",
                    null, AuthorizationType.READ
            );
            Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Account type cannot be null.");
        }

        @Test
        @DisplayName("Should fail validation when authorizationType is null")
        void validate_withNullAuthorizationType_shouldFail() {
            PowerOfAttorney poa = new PowerOfAttorney(
                    "poa-id-1", "grantor-1", "grantee-1", "NL01RABO0123456789",
                    AccountType.PAYMENT, null
            );
            Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Authorization type cannot be null.");
        }
    }

    @Nested
    @DisplayName("Multiple Field Validation")
    class MultipleFieldValidation {
        @Test
        @DisplayName("Should report all violations when multiple fields are invalid")
        void validate_withMultipleInvalidFields_shouldReturnAllViolations() {
            PowerOfAttorney poa = new PowerOfAttorney(" ", " ", " ", " ", null, null);
            Set<ConstraintViolation<PowerOfAttorney>> violations = validator.validate(poa);
            assertThat(violations).hasSize(6);

            Set<String> expectedMessages = Set.of(
                    "Power of Attorney ID cannot be null or empty.",
                    "Grantor ID cannot be null or empty.",
                    "Grantee ID cannot be null or empty.",
                    "Account number must be a valid format.",
                    "Account type cannot be null.",
                    "Authorization type cannot be null."
            );

            assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrderElementsOf(expectedMessages);
        }
    }
}