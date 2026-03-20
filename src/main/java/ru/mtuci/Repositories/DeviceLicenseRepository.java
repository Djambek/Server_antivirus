package ru.mtuci.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.Entities.Device;
import ru.mtuci.Entities.DeviceLicense;
import ru.mtuci.Entities.License;

import java.util.UUID;

@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    long countByLicense(License license);
    boolean existsByLicenseAndDevice(License license, Device device);
}