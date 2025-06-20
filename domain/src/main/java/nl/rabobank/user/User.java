package nl.rabobank.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    @NotBlank(message = "Name cannot be empty.")
    @Size(max = 100)
    private String name;
    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Email should be a valid email format.")
    private String email;
    @NotBlank(message = "Password cannot be empty.")
    private String password;
}