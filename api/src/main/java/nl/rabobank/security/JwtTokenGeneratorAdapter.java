package nl.rabobank.security;

import lombok.RequiredArgsConstructor;
import nl.rabobank.dto.CustomUserDetails;
import nl.rabobank.user.ITokenGenerator;
import nl.rabobank.user.User;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

@Component
@RequiredArgsConstructor
public class JwtTokenGeneratorAdapter implements ITokenGenerator {
    private final JwtService jwtService;

    @Override
    public String generateTokenForUser(User user) {
        UserDetails userDetails = new CustomUserDetails(user);
        return jwtService.generateToken(userDetails);
    }
}