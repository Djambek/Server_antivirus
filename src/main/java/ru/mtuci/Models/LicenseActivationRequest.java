package ru.mtuci.Models;

import lombok.Data;

@Data
public class LicenseActivationRequest {
    private String activationCode;
    private String deviceMacAddress;
    private String deviceName;
}