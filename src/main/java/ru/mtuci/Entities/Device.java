package ru.mtuci.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "device")
@Getter @Setter @NoArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    @Column(unique = true, nullable = false)
    private String mac_address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;
}