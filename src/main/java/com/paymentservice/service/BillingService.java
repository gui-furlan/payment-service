package com.paymentservice.service;

import com.paymentservice.client.TicketClient;
import com.paymentservice.model.Billing;
import com.paymentservice.model.enums.BillingStatus;
import com.paymentservice.repository.BillingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private final BillingRepository billingRepository;
    private final TicketClient ticketClient;

    public BillingService(BillingRepository billingRepository, TicketClient ticketClient) {
        this.billingRepository = billingRepository;
        this.ticketClient = ticketClient;
    }

    public Billing createBilling(BigDecimal value, UUID ticketId, BillingStatus status, LocalDateTime dueDate) {
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
    public List<Billing> listByTicketId(UUID ticketId) {
        List<Billing> billings = billingRepository.findByTicketId(ticketId);
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
