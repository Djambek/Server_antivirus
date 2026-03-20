package ru.mtuci.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "license_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private Integer default_duration_in_days;
    private String description;
}