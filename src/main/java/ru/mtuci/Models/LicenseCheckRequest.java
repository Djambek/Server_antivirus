package ru.mtuci.Models;

import lombok.Data;

import java.util.UUID;

@Data
public class LicenseCheckRequest {
    private String activationCode;
    private String deviceMacAddress;
    private String deviceName;
    private UUID productId;
}