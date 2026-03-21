package ru.mtuci.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mtuci.Entities.ApplicationUser;
import ru.mtuci.Entities.Device;
import ru.mtuci.Entities.License;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findByCode(String code);

    @Query("SELECT l FROM License l " +
            "JOIN DeviceLicense dl ON l.id = dl.license.id " +
            "WHERE dl.device = :device " +
            "AND l.user = :user " +
            "AND l.product.id = :productId " +
            "AND l.blocked = false " +
            "AND l.ending_date > CURRENT_TIMESTAMP")
    Optional<License> findActiveByDeviceUserAndProduct(
            @Param("device") Device device,
            @Param("user") ApplicationUser user,
            @Param("productId") UUID productId
    );
}