package com.paymentservice.repository;

import com.paymentservice.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillingRepository extends JpaRepository<Billing, UUID> {
    List<Billing> findByTicketId(UUID ticketId);
}
