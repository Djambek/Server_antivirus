package ru.mtuci.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.Entities.ApplicationUser;
import ru.mtuci.Entities.License;
import ru.mtuci.Models.LicenseCreateRequest;
import ru.mtuci.Services.LicenseService;

@RestController
@RequestMapping("/api/licenses")
@Tag(name = "Лицензии")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание новой лицензии администратором")
    public ResponseEntity<License> createLicense(
            @RequestBody LicenseCreateRequest request,
            @AuthenticationPrincipal ApplicationUser admin) {

        License createdLicense = licenseService.createLicense(request, admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLicense);
    }
}