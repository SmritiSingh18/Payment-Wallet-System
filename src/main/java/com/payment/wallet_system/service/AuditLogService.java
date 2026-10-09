
package com.payment.wallet_system.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.AuditLog;
import com.payment.wallet_system.respository.AuditLogRespository;

@Service
public class AuditLogService {

    private final AuditLogRespository auditLogRespository;

    public AuditLogService(AuditLogRespository auditLogRespository) {
        this.auditLogRespository = auditLogRespository;
    }

    // Successful audit logs participate in the current transaction.
    @Transactional(propagation = Propagation.REQUIRED)
    public void log(
            Long userId,
            AuditEventType eventType,
            String description,
            Long transactionId) {

        saveAuditLog(userId, eventType, description, transactionId);
    }

    // Failed-transfer audit logs survive the transfer rollback.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(
            Long userId,
            AuditEventType eventType,
            String description,
            Long transactionId) {

        saveAuditLog(userId, eventType, description, transactionId);
    }

    private void saveAuditLog(
            Long userId,
            AuditEventType eventType,
            String description,
            Long transactionId) {

        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setEventType(eventType);
        auditLog.setDescription(description);
        auditLog.setTransactionId(transactionId);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRespository.save(auditLog);
    }
}
