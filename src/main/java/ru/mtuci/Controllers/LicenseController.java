package ru.mtuci.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.mtuci.Entities.ApplicationUser;
import ru.mtuci.Entities.License;
import ru.mtuci.Models.LicenseActivationRequest;
import ru.mtuci.Models.LicenseCheckRequest;
import ru.mtuci.Models.LicenseCreateRequest;
import ru.mtuci.Models.TicketResponse;
import ru.mtuci.Services.LicenseService;

import java.util.Map;

@RestController
@RequestMapping("/api/licenses")
@Tag(name = "Лицензии")
@RequiredArgsConstructor
@Slf4j
public class LicenseController {

    private final LicenseService licenseService;

    @PostMapping
    @Operation(summary = "Создание новой лицензии администратором")
    public ResponseEntity<?> createLicense(
            @RequestBody LicenseCreateRequest request, @AuthenticationPrincipal ApplicationUser admin) {
        try {
            log.info("Inside creation");
            License createdLicense = licenseService.createLicense(request, admin);
            log.info("Created");
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLicense);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Возвращаем ошибку с соответствующим статусом (403, 404, 409) и сообщением
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            // Обработка непредвиденных ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
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

    @PostMapping("/check")
    @Operation(summary = "Проверка существующей лицензии")
    public ResponseEntity<?> checkLicense(
            @RequestBody LicenseCheckRequest request,
            @AuthenticationPrincipal ApplicationUser user) {
        try {
            TicketResponse response = licenseService.checkLicense(request, user);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("/renew")
    @Operation(summary = "Продление существующей лицензии")
    public ResponseEntity<?> renewLicense(
            @RequestBody LicenseActivationRequest request,
            @AuthenticationPrincipal ApplicationUser user) {
        try {
            TicketResponse response = licenseService.renewLicense(request, user);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}