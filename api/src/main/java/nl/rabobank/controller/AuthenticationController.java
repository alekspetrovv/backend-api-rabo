package nl.rabobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.rabobank.dto.LoginDto;
import nl.rabobank.dto.ReadLoginDto;
import nl.rabobank.dto.RegisterDto;
import nl.rabobank.user.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ReadLoginDto> register(
            @Valid @RequestBody RegisterDto registerDto
    ) {
        String token = authenticationService.register(registerDto.getEmail(), registerDto.getName(), registerDto.getPassword());
        return new ResponseEntity<>(new ReadLoginDto(token), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ReadLoginDto> login(
            @Valid @RequestBody LoginDto loginDto
    ) {
        String token = authenticationService.login(loginDto.getEmail(), loginDto.getPassword());
        return ResponseEntity.ok(new ReadLoginDto(token));
    }
}