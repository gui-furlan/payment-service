package com.paymentservice.repository;

import com.paymentservice.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//import java.util.UUID;

public interface BillingTicketRepository extends JpaRepository<Billing, String> {
    List<Billing> findByTicketId(String ticketId);
}
