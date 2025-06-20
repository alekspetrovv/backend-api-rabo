package nl.rabobank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.rabobank.account.AccountType;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadAccountDto {
    private String accountNumber;
    private BigDecimal balance;
    private String ownerId;
    private AccountType type;
}