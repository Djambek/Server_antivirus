package ru.mtuci.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "license")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private LicenseType type;

    private Date first_activation_date;
    private Date ending_date;
    private boolean blocked;
    private Integer device_count;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private ApplicationUser owner;

    private String description;
}