package com.paymentservice.model;

import com.paymentservice.model.enums.BillingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "billings")
public class Billing {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingStatus status;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Billing() {}

    public Billing(UUID id, BigDecimal value, String ticketId, BillingStatus status,
                   LocalDateTime dueDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.value = value;
        this.ticketId = ticketId;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = BillingStatus.PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public BillingStatus getStatus() {
        return status;
    }

    public void setStatus(BillingStatus status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
