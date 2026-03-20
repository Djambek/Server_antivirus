package ru.mtuci.Models;

import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class Ticket {
    private Date serverDate;          // Текущая дата сервера
    private long ticketLifetime;      // Время жизни тикета (в миллисекундах)
    private Date activationDate;      // Дата активации лицензии
    private Date expirationDate;      // Дата истечения лицензии
    private UUID userId;              // Идентификатор пользователя
    private UUID deviceId;            // Идентификатор устройства
    private boolean isBlocked;        // Флаг блокировки лицензии
}