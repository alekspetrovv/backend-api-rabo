package nl.rabobank.user;

import lombok.RequiredArgsConstructor;

import nl.rabobank.exception.RecordNotFoundException;
import nl.rabobank.exception.UserAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenGenerator tokenGenerator;
    private final IAuthenticationPort authenticationPort;

    @Transactional
    public String register(String email, String name, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use.");
        }

        User newUser = new User(
                randomUUID().toString(),
                name,
                email,
                passwordHasher.hashPassword(rawPassword)
        );

        userRepository.save(newUser);

        return tokenGenerator.generateTokenForUser(newUser);
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        authenticationPort.authenticate(email, password);

        User authenticatedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RecordNotFoundException("User with email not found: " + email));

        return tokenGenerator.generateTokenForUser(authenticatedUser);
    }
}