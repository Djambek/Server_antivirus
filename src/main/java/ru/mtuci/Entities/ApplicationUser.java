package ru.mtuci.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @JsonIgnore
    @Column(unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash")
    private String password;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    private boolean isAccountExpired;



    @JsonIgnore
    private boolean isAccountLocked;

    @JsonIgnore
    private boolean isCredentialsExpired;

    @JsonIgnore
    private boolean isDisabled;

    @JsonIgnore
    @Override
    public Collection<? extends Role> getAuthorities() {
        return Collections.singletonList(role);
    }

    @JsonIgnore
    @Override
    public String getUsername() { return email; }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() { return !isAccountExpired; }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() { return !isAccountLocked; }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() { return !isCredentialsExpired; }

    @JsonIgnore
    @Override
    public boolean isEnabled() { return !isDisabled; }
}