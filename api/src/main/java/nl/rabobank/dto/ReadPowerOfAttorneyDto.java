package nl.rabobank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadPowerOfAttorneyDto {
    private String id;
    private String grantorId;
    private String granteeId;
    private String accountNumber;
    private AccountType accountType;
    private AuthorizationType authorizationType;
    private Instant createdAt;
    private Instant lastModifiedAt;
}