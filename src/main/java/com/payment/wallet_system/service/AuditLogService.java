package com.payment.wallet_system.service;

import java.time.LocalDateTime;

import org.springframework.data.projection.EntityProjection.ProjectionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.AuditLog;
import com.payment.wallet_system.respository.AuditLogRespository;


@Service 
public class AuditLogService {
    private  final AuditLogRespository auditLogRespository;
    public AuditLogService(AuditLogRespository auditLogRespository){
        this.auditLogRespository=auditLogRespository;
    }
    
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public  void  log(Long userId,
                      AuditEventType eventType,
                      String decription,
                      Long transactionId){

    AuditLog auditLog=new AuditLog();

    auditLog.setUserId(userId);
    auditLog.setEventType(eventType);
    auditLog.setDescription(decription);
    auditLog.setTransactionId(transactionId);
    auditLog.setCreatedAt(LocalDateTime.now());

    auditLogRespository.save(auditLog);
  }
    
}
