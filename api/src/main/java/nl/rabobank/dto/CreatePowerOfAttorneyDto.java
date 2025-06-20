package nl.rabobank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;

@Getter
@Setter
public class CreatePowerOfAttorneyDto {
    @NotBlank(message = "Grantee ID cannot be null or empty.")
    private String granteeId;

    @NotBlank(message = "Account number cannot be null or empty.")
    @Pattern(regexp = "^NL\\d{2}RABO\\d{10}$", message = "Account number must be a valid format (e.g., NLxxRABOxxxxxxxxxx).")
    private String accountNumber;

    @NotNull(message = "Account type cannot be null.")
    private AccountType accountType;

    @NotNull(message = "Authorization type cannot be null.")
    private AuthorizationType authorizationType;
}