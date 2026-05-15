package ru.mtuci.Models;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class BatchSignatureRequest {
    private List<UUID> ids;
}