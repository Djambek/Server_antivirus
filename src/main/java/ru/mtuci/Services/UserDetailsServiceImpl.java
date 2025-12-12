package ru.mtuci.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mtuci.Repositories.ApplicationUserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ApplicationUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Попытка входа пользователя с email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }
}