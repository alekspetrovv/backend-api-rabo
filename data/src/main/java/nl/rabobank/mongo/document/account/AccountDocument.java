package nl.rabobank.mongo.document.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.account.AccountType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "accounts")
public class AccountDocument {
    @Id
    private String accountNumber;
    @Indexed
    @Field("owner_id")
    private String ownerId;
    private AccountType type;
    private BigDecimal balance;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant lastModifiedAt;
}