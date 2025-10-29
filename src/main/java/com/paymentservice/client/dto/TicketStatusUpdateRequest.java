package com.paymentservice.client.dto;

import com.paymentservice.model.enums.BillingStatus;

import java.util.UUID;

public record TicketStatusUpdateRequest(
        UUID ticketId,
        BillingStatus status
) {}

