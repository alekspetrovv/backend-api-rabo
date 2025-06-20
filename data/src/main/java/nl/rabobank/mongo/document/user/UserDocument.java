package nl.rabobank.mongo.document.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserDocument {
        @Id
        private String id;
        private String name;
        @Indexed(unique = true)
        private String email;
        private String password;
}