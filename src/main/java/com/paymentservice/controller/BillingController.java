package com.paymentservice.controller;

import com.paymentservice.model.Billing;
import com.paymentservice.model.enums.BillingStatus;
import com.paymentservice.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/billings")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    public ResponseEntity<Billing> create(@RequestBody CreateBillingRequest body) {
        Billing created = billingService.createBilling(
                body.value(),
                body.ticketId(),
                body.status(),
                body.dueDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<Billing>> listByTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(billingService.listByTicketId(ticketId));
    }

    public record CreateBillingRequest(
            BigDecimal value,
            UUID ticketId,
            BillingStatus status,
            LocalDateTime dueDate
    ) { }
}
