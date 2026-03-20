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
import ru.mtuci.Models.LicenseActivationRequest;
import ru.mtuci.Models.LicenseCreateRequest;
import ru.mtuci.Models.TicketResponse;
import ru.mtuci.Services.LicenseService;

import java.util.Map;

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

    @PostMapping("/activate")
    @Operation(summary = "Активация лицензии на устройстве пользователя")
    public ResponseEntity<?> activateLicense(
            @RequestBody LicenseActivationRequest request,
            @AuthenticationPrincipal ApplicationUser user) {
        try {
            // Вызов сервиса для выполнения бизнес-логики активации
            TicketResponse response = licenseService.activateLicense(request, user);
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Возвращаем ошибку с соответствующим статусом (403, 404, 409) и сообщением
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            // Обработка непредвиденных ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}