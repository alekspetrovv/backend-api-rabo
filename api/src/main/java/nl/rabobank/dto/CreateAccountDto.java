package nl.rabobank.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nl.rabobank.account.AccountType;

@Getter
@Setter
public class CreateAccountDto {
    @NotNull(message = "Account type cannot be null.")
    private AccountType type;
}