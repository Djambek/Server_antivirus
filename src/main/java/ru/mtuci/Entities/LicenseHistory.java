package ru.mtuci.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "license_history")
@Getter @Setter @NoArgsConstructor
public class LicenseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    private String status;
    private Date change_date;
    private String description;
}