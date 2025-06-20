package nl.rabobank.mongo.document.attorney;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.AuthorizationType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "power_of_attorneys")
@CompoundIndexes({
    @CompoundIndex(name = "unique_poa_grant", def = "{'grantorId': 1, 'granteeId': 1, 'accountNumber': 1, 'authorizationType': 1}", unique = true),
    @CompoundIndex(name = "poa_by_grantor", def = "{'grantorId': 1}"),
    @CompoundIndex(name = "poa_by_grantee", def = "{'granteeId': 1}"),
    @CompoundIndex(name = "poa_by_account", def = "{'accountNumber': 1}")
})
public class PowerOfAttorneyDocument {

    @Id
    private String id;

    @Field("grantor_id")
    @Indexed
    private String grantorId;

    @Field("grantee_id")
    @Indexed
    private String granteeId;

    @Field("account_number")
    @Indexed
    private String accountNumber;

    @Field("account_type")
    private AccountType accountType;

    @Field("authorization_type")
    private AuthorizationType authorizationType;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant lastModifiedAt;
}