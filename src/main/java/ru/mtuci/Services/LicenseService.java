package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.Entities.*;
import ru.mtuci.Models.LicenseCreateRequest;
import ru.mtuci.Repositories.LicenseHistoryRepository;
import ru.mtuci.Repositories.LicenseRepository;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final UserDetailsServiceImpl userDetailsService;
    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;

    @Transactional
    public License createLicense(LicenseCreateRequest request, ApplicationUser admin) {
        // 404
        Product product = productService.getProductOrFail(request.getProductId());
        LicenseType licenseType = licenseTypeService.getTypeOrFail(request.getTypeId());
        ApplicationUser ownerUser = userDetailsService.getActiveUserOrFail(request.getOwnerId());

        // 2. Создание лицензии
        License license = new License();
        license.setCode(generateCode());
        license.setProduct(product);
        license.setType(licenseType);
        license.setOwner(ownerUser);
        license.setUser(null);
        license.setDevice_count(request.getDeviceCount() != null ? request.getDeviceCount() : 1);
        license.setBlocked(false);
        license.setDescription("License created by admin");

        License savedLicense = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(savedLicense);
        history.setUser(admin);
        history.setStatus("CREATED");
        history.setChange_date(new Date());
        history.setDescription("Initial license creation");

        licenseHistoryRepository.save(history);

        return savedLicense;
    }

    private String generateCode() {
        return UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 16);
    }
}