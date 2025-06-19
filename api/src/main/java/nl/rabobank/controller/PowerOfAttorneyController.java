package nl.rabobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.rabobank.account.AuthorizationType;
import nl.rabobank.attorney.PowerOfAttorney;
import nl.rabobank.dto.CreatePowerOfAttorneyDto;
import nl.rabobank.dto.CustomUserDetails;
import nl.rabobank.dto.ReadPowerOfAttorneyDto;
import nl.rabobank.service.PowerOfAttorneyService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/power-of-attorneys")
@RequiredArgsConstructor
public class PowerOfAttorneyController {
    private final PowerOfAttorneyService powerOfAttorneyService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ReadPowerOfAttorneyDto> createPowerOfAttorney(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreatePowerOfAttorneyDto createPowerOfAttorneyDto
    ) {
        PowerOfAttorney createdPowerOfAttorney = powerOfAttorneyService.createPowerOfAttorney(currentUser.id(), createPowerOfAttorneyDto);
        ReadPowerOfAttorneyDto responseDto = modelMapper.map(createdPowerOfAttorney, ReadPowerOfAttorneyDto.class);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/my-grants")
    public ResponseEntity<List<ReadPowerOfAttorneyDto>> getMyAccessGrants(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Optional<AuthorizationType> permission
    ) {
        List<PowerOfAttorney> grants = powerOfAttorneyService.getAccessGrantsForGrantee(currentUser.id(), permission);

        List<ReadPowerOfAttorneyDto> readPowerAttorneyDtos = grants.stream()
                .map(poa -> modelMapper.map(poa, ReadPowerOfAttorneyDto.class))
                .collect(Collectors.toList());

        return new ResponseEntity<>(readPowerAttorneyDtos, HttpStatus.OK);
    }
}