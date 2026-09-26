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

@Entity 
@Data 
@Table (name = "audit_logs")
public class AuditLog {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column (nullable = false)
    private Long userId;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private AuditEventType eventType;

    @Column (nullable = false)
    private String description;

    
    private  Long  transactionId;

    @Column (nullable = false)
    private  LocalDateTime createdAt;
}
