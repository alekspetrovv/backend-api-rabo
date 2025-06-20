package nl.rabobank.service;


import lombok.RequiredArgsConstructor;
import nl.rabobank.dto.CustomUserDetails;
import nl.rabobank.exception.UserAlreadyExistsException;
import nl.rabobank.mongo.document.user.UserDocument;
import nl.rabobank.mongo.repository.UserRepository;
import nl.rabobank.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ModelMapper modelMapper;

    public String register(String email, String name, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use.");
        }

        User newUser = new User(
                randomUUID().toString(),
                name,
                email,
                passwordEncoder.encode(rawPassword)
        );

        UserDocument userDoc = modelMapper.map(newUser, UserDocument.class);
        userRepository.save(userDoc);

        UserDetails userDetails = new CustomUserDetails(userDoc);
        return jwtService.generateToken(userDetails);
    }

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateToken(userDetails);
    }
}