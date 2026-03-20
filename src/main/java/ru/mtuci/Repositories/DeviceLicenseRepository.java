package ru.mtuci.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.Entities.DeviceLicense;

import java.util.UUID;

@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
}