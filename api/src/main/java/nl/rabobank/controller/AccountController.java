package nl.rabobank.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.dto.CreateAccountDto;
import nl.rabobank.dto.CustomUserDetails;
import nl.rabobank.dto.ReadAccountDto;
import nl.rabobank.account.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ReadAccountDto> createAccount(@Valid @RequestBody CreateAccountDto createAccountDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Account createdAccount = accountService.createAccount(
                createAccountDto.getType(),
                userDetails.id()
        );

        ReadAccountDto responseBody = modelMapper.map(createdAccount, ReadAccountDto.class);

        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }
}