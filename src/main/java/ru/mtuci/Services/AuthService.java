package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mtuci.Entities.ApplicationUser;
import ru.mtuci.Entities.Role;
import ru.mtuci.Models.RegisterRequest;
import ru.mtuci.Repositories.ApplicationUserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }

        if (!isValidPassword(request.getPassword())) {
            throw new RuntimeException("Password must be at least 8 characters long and contain special symbols (!@#$%^&*)");
        }

        ApplicationUser user = new ApplicationUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER); // По дефолту даем роль USER

        userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[!@#$%^&*]).{8,}$");
    }
}
