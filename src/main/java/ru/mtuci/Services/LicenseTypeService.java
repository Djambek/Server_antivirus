package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.mtuci.Entities.LicenseType;
import ru.mtuci.Repositories.LicenseTypeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseTypeService {
    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseType getTypeOrFail(UUID typeId) {
        return licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "License type not found"));
    }
}