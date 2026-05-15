package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mtuci.Entities.*;
import ru.mtuci.Models.*;
import ru.mtuci.Repositories.*;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {

    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final UserDetailsServiceImpl userDetailsService;
    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;

    private final DeviceRepository  deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;

    private final SignatureService signatureService;

    @Transactional
    public License createLicense(LicenseCreateRequest request, ApplicationUser admin) {
        // 404
        log.info("Function create license started");
        Product product = productService.getProductOrFail(request.getProductId());
        LicenseType licenseType = licenseTypeService.getTypeOrFail(request.getTypeId());
        ApplicationUser ownerUser = userDetailsService.getActiveUserOrFail(request.getOwnerId());
        log.info("all product founded");
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

    @Transactional
    public TicketResponse activateLicense(LicenseActivationRequest request, ApplicationUser currentUser) {
        // 1. Поиск лицензии по коду
        log.info("Внутри активации");
        License license = licenseRepository.findByCode(request.getActivationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "License not found"));
        log.info("лизенция существует");
        // 2. Блок [license.user != null and license.user.id != userId] -> 403 Forbidden
        if (license.getUser() != null && !license.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "License owned by another user");
        }
        log.info("После проверки пользака");
        // 3. Поиск или регистрация устройства по MAC
        Device device = deviceRepository.findByMacAddress(request.getDeviceMacAddress())
                .map(existingDevice -> {
                    if (request.getDeviceName() != null && !request.getDeviceName().isBlank()) {
                        existingDevice.setName(request.getDeviceName());
                    }
                    return deviceRepository.save(existingDevice);
                })
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setMac_address(request.getDeviceMacAddress());
                    newDevice.setName(request.getDeviceName() != null ? request.getDeviceName() : "Device_" + request.getDeviceMacAddress());
                    newDevice.setUser(currentUser);
                    return deviceRepository.save(newDevice);
                });
        log.info("Нашли девайс");
        // 4. Логика активации (первая или повторная)
        if (license.getUser() == null) {
            license.setUser(currentUser);
            license.setFirst_activation_date(new Date());

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, license.getType().getDefault_duration_in_days()); //
            license.setEnding_date(cal.getTime());
        }

        // 5. Проверка лимита устройств (check device count limit)
        if (!deviceLicenseRepository.existsByLicenseAndDevice(license, device)) {
            if (deviceLicenseRepository.countByLicense(license) >= license.getDevice_count()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Device limit reached");
            }

            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setLicense(license);
            deviceLicense.setDevice(device);
            deviceLicense.setActivation_date(new Date());
            deviceLicenseRepository.save(deviceLicense);
        }

        License savedLicense = licenseRepository.save(license);

        // 6. Запись в историю (status=ACTIVATED)
        LicenseHistory history = new LicenseHistory();
        history.setLicense(savedLicense);
        history.setUser(currentUser);
        history.setStatus("ACTIVATED");
        history.setChange_date(new Date());
        history.setDescription("Activated on device: " + device.getMac_address());
        licenseHistoryRepository.save(history);

        // 7. Формирование Тикета (build Ticket)
        Ticket ticket = Ticket.builder()
                .serverDate(new Date())
                .ticketLifetime(900000L) // Например, 15 минут (accessExpiration)
                .activationDate(savedLicense.getFirst_activation_date())
                .expirationDate(savedLicense.getEnding_date())
                .userId(currentUser.getId())
                .deviceId(device.getId())
                .isBlocked(savedLicense.isBlocked())
                .build();

        // В будущем здесь добавится логика подписи (signature) через KeyStore
        String signature = signatureService.signTicket(ticket);

        return new TicketResponse(ticket, signature);
    }

    public TicketResponse checkLicense(LicenseCheckRequest request, ApplicationUser currentUser) {
        // 1. findByMac(request.deviceMac)
        Device device = deviceRepository.findByMacAddress(request.getDeviceMacAddress())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        // 2. findActiveByDeviceUserAndProduct(...)
        // Примечание: В запросе LicenseActivationRequest нет productId,
        // для соответствия диаграмме может потребоваться расширить модель или передавать его иначе.
        // Здесь предполагается использование ID продукта из логики приложения.
        Product product = productService.getProductOrFail(request.getProductId());
        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                device,
                currentUser,
                product.getId()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active license not found"));

        // 3. build Ticket(license)
        Ticket ticket = Ticket.builder()
                .serverDate(new Date())
                .ticketLifetime(900000L)
                .activationDate(license.getFirst_activation_date())
                .expirationDate(license.getEnding_date())
                .userId(currentUser.getId())
                .deviceId(device.getId())
                .isBlocked(license.isBlocked())
                .build();

        String signature = signatureService.signTicket(ticket);

        return new TicketResponse(ticket, signature);
    }

    @Transactional
    public TicketResponse renewLicense(LicenseActivationRequest request, ApplicationUser currentUser) {
        // 1. findByCodeOrFail (используем существующий репозиторий)
        License license = licenseRepository.findByCode(request.getActivationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "License not found"));

        // 2. check renewability (inactive or expires <= 7 days)
        Date now = new Date();
        long sevenDaysInMs = 7L * 24 * 60 * 60 * 1000;
        boolean isExpiredOrSoonExpiring = license.getEnding_date() != null &&
                (license.getEnding_date().before(now) ||
                        (license.getEnding_date().getTime() - now.getTime() <= sevenDaysInMs));

        // Либо она еще не активирована (user == null), либо скоро истекает
        if (license.getUser() != null && !isExpiredOrSoonExpiring) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Renewal not allowed: license is still active");
        }

        // 3. extend endingDate by license.type.defaultDuration
        Calendar cal = Calendar.getInstance();
        // Если лицензия уже истекла, продлеваем от текущей даты, если нет — от даты окончания
        if (license.getEnding_date() != null && license.getEnding_date().after(now)) {
            cal.setTime(license.getEnding_date());
        } else {
            cal.setTime(now);
        }
        cal.add(Calendar.DAY_OF_YEAR, license.getType().getDefault_duration_in_days());
        license.setEnding_date(cal.getTime());

        // 4. Transaction: save(license)
        License savedLicense = licenseRepository.save(license);

        // 5. Transaction: save(history: статус=RENEWED)
        LicenseHistory history = new LicenseHistory();
        history.setLicense(savedLicense);
        history.setUser(currentUser);
        history.setStatus("RENEWED");
        history.setChange_date(new Date());
        history.setDescription("License renewed by user");
        licenseHistoryRepository.save(history);

        // 6. build Ticket(license)
        Ticket ticket = Ticket.builder()
                .serverDate(new Date())
                .ticketLifetime(900000L)
                .activationDate(savedLicense.getFirst_activation_date())
                .expirationDate(savedLicense.getEnding_date())
                .userId(currentUser.getId())
                .isBlocked(savedLicense.isBlocked())
                // deviceId можно получить из истории или оставить null, если продление общее
                .build();

        return new TicketResponse(ticket, "digital_signature_placeholder");
    }
}