package ru.mtuci.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mtuci.Entities.ApplicationUser;
import ru.mtuci.Entities.Device;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    @Query("SELECT d FROM Device d WHERE d.mac_address = :mac")
    Optional<Device> findByMacAddress(@Param("mac") String mac);
}
