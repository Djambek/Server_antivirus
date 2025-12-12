package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mtuci.Entities.ApplicationUser;
import ru.mtuci.Entities.Role;
import ru.mtuci.Entities.SessionStatus;
import ru.mtuci.Entities.UserSession;
import ru.mtuci.Models.RegisterRequest;
import ru.mtuci.Models.AuthenticationResponse;
import ru.mtuci.Models.LoginRequest;
import ru.mtuci.Repositories.ApplicationUserRepository;
import ru.mtuci.Repositories.UserSessionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionRepository sessionRepository;
    private final JwtProvider jwtService;
    private final AuthenticationManager authenticationManager;

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

    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserSession(user, refreshToken);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (session.getStatus() == SessionStatus.CLOSED || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        ApplicationUser user = session.getUser();
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid token format");
        }

        session.setStatus(SessionStatus.CLOSED);
        sessionRepository.save(session);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        saveUserSession(user, newRefreshToken);

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }

    private void saveUserSession(ApplicationUser user, String refreshToken) {
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .status(SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(jwtService.getRefreshExpiration(), ChronoUnit.MILLIS))
                .build();
        sessionRepository.save(session);
    }

}
