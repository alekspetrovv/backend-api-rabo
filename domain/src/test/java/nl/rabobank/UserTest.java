package nl.rabobank;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import nl.rabobank.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("User Domain Class Tests")
class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation for a completely valid user")
    void validate_withValidUser_shouldHaveNoViolations() {
        User user = new User("id-1", "Test User", "test@rabobank.nl", "password123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Setters and getters should function correctly")
    void settersAndGetters_shouldFunctionCorrectly() {
        User user = new User();
        user.setId("id-2");
        user.setName("Another User");
        user.setEmail("another@rabobank.nl");
        user.setPassword("anotherPass");

        assertThat(user.getId()).isEqualTo("id-2");
        assertThat(user.getName()).isEqualTo("Another User");
        assertThat(user.getEmail()).isEqualTo("another@rabobank.nl");
        assertThat(user.getPassword()).isEqualTo("anotherPass");
    }

    @Nested
    @DisplayName("Name Validation")
    class NameValidation {
        @Test
        @DisplayName("Should fail validation when name is null")
        void validate_withNullName_shouldFail() {
            User user = new User("id-1", null, "test@rabobank.nl", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be empty.");
        }

        @Test
        @DisplayName("Should fail validation when name is empty")
        void validate_withEmptyName_shouldFail() {
            User user = new User("id-1", "", "test@rabobank.nl", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be empty.");
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void validate_withBlankName_shouldFail() {
            User user = new User("id-1", "   ", "test@rabobank.nl", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be empty.");
        }

        @Test
        @DisplayName("Should fail validation when name exceeds 100 characters")
        void validate_withNameTooLong_shouldFail() {
            String longName = "a".repeat(101);
            User user = new User("id-1", longName, "test@rabobank.nl", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("size must be between 0 and 100");
        }

        @Test
        @DisplayName("Should pass validation when name is exactly 100 characters")
        void validate_withNameAtMaxLength_shouldSucceed() {
            String maxName = "a".repeat(100);
            User user = new User("id-1", maxName, "test@rabobank.nl", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Email Validation")
    class EmailValidation {
        @Test
        @DisplayName("Should fail validation when email is null")
        void validate_withNullEmail_shouldFail() {
            User user = new User("id-1", "Test User", null, "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be empty.");
        }

        @Test
        @DisplayName("Should fail validation when email is empty")
        void validate_withEmptyEmail_shouldFail() {
            User user = new User("id-1", "Test User", "", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).hasSize(1);

            assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                    "Email cannot be empty.");
        }

        @Test
        @DisplayName("Should fail validation for email without '@' symbol")
        void validate_withMalformedEmail_noAtSign_shouldFail() {
            User user = new User("id-1", "Test User", "testrabobank.nl", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Email should be a valid email format.");
        }

        @Test
        @DisplayName("Should fail validation for email without domain")
        void validate_withMalformedEmail_noDomain_shouldFail() {
            User user = new User("id-1", "Test User", "test@", "password123");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Email should be a valid email format.");
        }
    }

    @Nested
    @DisplayName("Password Validation")
    class PasswordValidation {
        @Test
        @DisplayName("Should fail validation when password is null")
        void validate_withNullPassword_shouldFail() {
            User user = new User("id-1", "Test User", "test@rabobank.nl", null);
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Password cannot be empty.");
        }

        @Test
        @DisplayName("Should fail validation when password is blank")
        void validate_withBlankPassword_shouldFail() {
            User user = new User("id-1", "Test User", "test@rabobank.nl", "   ");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Password cannot be empty.");
        }
    }

    @Nested
    @DisplayName("Multiple Field Validation")
    class MultipleFieldValidation {
        @Test
        @DisplayName("Should report all violations when multiple fields are invalid")
        void validate_withMultipleInvalidFields_shouldReturnAllViolations() {
            User user = new User("id-1", " ", "invalid-email", "");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).hasSize(3);

            Set<String> messages = Set.of(
                "Name cannot be empty.",
                "Email should be a valid email format.",
                "Password cannot be empty."
            );

            assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrderElementsOf(messages);
        }
    }
}