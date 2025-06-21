package nl.rabobank.security;

import lombok.RequiredArgsConstructor;
import nl.rabobank.user.IAuthenticationPort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityAuthenticationAdapter implements IAuthenticationPort {
    private final AuthenticationManager authenticationManager;

    @Override
    public void authenticate(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }
}