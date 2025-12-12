package ru.mtuci.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.Entities.ApplicationUser;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, UUID> {
    Optional<ApplicationUser> findByEmail(String email);
}