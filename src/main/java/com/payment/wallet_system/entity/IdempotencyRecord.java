package com.payment.wallet_system.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity 
@Table (name = "idempotency_records")
public class IdempotencyRecord {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true,nullable = false)
    private String idempotencyKey;

    @Column (nullable = false)
    private String transactionId;

    @Column (nullable = false)
    private Long userId;

    @Column (nullable = false)
    @Enumerated (EnumType.STRING)
    private IdempotencyStatus status;

    @Column (nullable = false)
    private LocalDateTime createdAt;
}
