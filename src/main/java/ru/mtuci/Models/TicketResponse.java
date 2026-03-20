package ru.mtuci.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketResponse {
    private Ticket ticket;
    private String signature; // ЭЦП на основе данных тикета
}