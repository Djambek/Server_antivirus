package ru.mtuci.Models;

import lombok.Data;
import java.util.UUID;

@Data
public class LicenseCreateRequest {
    private UUID productId;
    private UUID typeId;
    private UUID ownerId;
    private Integer deviceCount;
}