package nl.rabobank.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    @NotBlank(message = "Email is mandatory")
    @Size(min = 3, max = 20, message = "Email must be between 3 and 20 characters")
    private String email;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
