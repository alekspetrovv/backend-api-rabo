package nl.rabobank.config;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.*;
import nl.rabobank.attorney.PowerOfAttorney;
import nl.rabobank.mongo.document.account.AccountDocument;
import nl.rabobank.mongo.document.attorney.PowerOfAttorneyDocument;
import nl.rabobank.security.CustomUserDetailsService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.createTypeMap(AccountDocument.class, Account.class)
                .setProvider(request -> {
                    AccountDocument source = (AccountDocument) request.getSource();
                    AccountType accountTypeFromDocument = AccountType.valueOf(source.getType());
                    if (accountTypeFromDocument == AccountType.PAYMENT) {
                        return new PaymentAccount(source.getAccountNumber(), source.getOwnerId(), accountTypeFromDocument);
                    } else if (accountTypeFromDocument == AccountType.SAVING) {
                        return new SavingsAccount(source.getAccountNumber(), source.getOwnerId(), accountTypeFromDocument);
                    }
                    throw new IllegalArgumentException("Unknown AccountType during AccountDocument to Account mapping: " + source.getType());
                });
        modelMapper.createTypeMap(PowerOfAttorneyDocument.class, PowerOfAttorney.class)
                .setConverter(context -> {
                    PowerOfAttorneyDocument source = context.getSource();
                    return new PowerOfAttorney(
                            source.getId(),
                            source.getGrantorId(),
                            source.getGranteeId(),
                            source.getAccountNumber(),
                            AccountType.valueOf(source.getAccountType()),
                            AuthorizationType.valueOf(source.getAuthorizationType())
                    );
                });
        return modelMapper;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}