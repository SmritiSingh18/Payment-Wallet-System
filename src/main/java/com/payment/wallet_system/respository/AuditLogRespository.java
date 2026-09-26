package com.payment.wallet_system.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.wallet_system.entity.AuditLog;

public interface AuditLogRespository extends JpaRepository<AuditLog,Long> {
    
}
