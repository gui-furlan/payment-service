package com.paymentservice.service;

import com.paymentservice.client.TicketClient;
import com.paymentservice.model.Billing;
import com.paymentservice.model.enums.BillingStatus;
import com.paymentservice.repository.BillingRepository;
import com.paymentservice.repository.BillingTicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private final BillingRepository billingRepository;
    private final BillingTicketRepository billingTicketRepository;
    private final TicketClient ticketClient;

    public BillingService(BillingRepository billingRepository, TicketClient ticketClient, BillingTicketRepository billingTicketRepository) {
        this.billingRepository = billingRepository;
        this.ticketClient = ticketClient;
        this.billingTicketRepository = billingTicketRepository;
    }

    public Billing createBilling(BigDecimal value, String ticketId, BillingStatus status, LocalDateTime dueDate) {
        Billing billing = new Billing();
        billing.setValue(value);
        billing.setTicketId(ticketId);
        billing.setStatus(status);
        billing.setDueDate(dueDate);
        return billingRepository.save(billing);
    }

    @Transactional
    public Billing updateBillingStatus(UUID billingId, BillingStatus newStatus) {
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new IllegalArgumentException("Billing not found: " + billingId));

        billing.setStatus(newStatus);
        Billing saved = billingRepository.save(billing);

        ticketClient.updateTicketStatus(saved.getTicketId(), newStatus);
        return saved;
    }

    @Transactional
    public List<Billing> listByTicketId(String ticketId) {
        List<Billing> billings = billingTicketRepository.findByTicketId(ticketId);
        LocalDateTime now = LocalDateTime.now();
        for (Billing billing : billings) {
            if (billing.getStatus() == BillingStatus.PENDING
                    && billing.getDueDate() != null
                    && !billing.getDueDate().isAfter(now)) {
                billing.setStatus(BillingStatus.EXPIRED);
                billingRepository.save(billing);
            }
        }
        return billings;
    }
}
