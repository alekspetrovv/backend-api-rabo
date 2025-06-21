package nl.rabobank.security;

import lombok.RequiredArgsConstructor;
import nl.rabobank.user.IPasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordHasher implements IPasswordHasher {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}