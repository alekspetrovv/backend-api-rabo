package nl.rabobank.attorney;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;

import java.time.Instant;

@Getter
public class PowerOfAttorney {

    @NotBlank(message = "Power of Attorney ID cannot be null or empty.")
    private final String id;

    @NotBlank(message = "Grantor ID cannot be null or empty.")
    private final String grantorId;

    @NotBlank(message = "Grantee ID cannot be null or empty.")
    private final String granteeId;

    @Pattern(regexp = "^NL\\d{2}RABO\\d{10}$", message = "Account number must be a valid format.")
    private final String accountNumber;

    @NotNull(message = "Account type cannot be null.")
    private final AccountType accountType;

    @NotNull(message = "Authorization type cannot be null.")
    @Setter
    private AuthorizationType authorizationType;

    private final Instant createdAt;
    private final Instant lastModifiedAt;

    public PowerOfAttorney(
            String id,
            String grantorId,
            String granteeId,
            String accountNumber,
            AccountType accountType,
            AuthorizationType authorizationType
    ) {
        this.id = id;
        this.grantorId = grantorId;
        this.granteeId = granteeId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.authorizationType = authorizationType;
        this.createdAt = Instant.now();
        this.lastModifiedAt = this.createdAt;
    }

    public PowerOfAttorney(
            String grantorId,
            String granteeId,
            String accountNumber,
            AccountType accountType,
            AuthorizationType authorizationType
    ) {
        this(java.util.UUID.randomUUID().toString(), grantorId, granteeId, accountNumber, accountType, authorizationType);
    }
}