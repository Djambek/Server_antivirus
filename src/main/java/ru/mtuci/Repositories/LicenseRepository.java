package ru.mtuci.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.Entities.License;

import java.util.UUID;

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {
}