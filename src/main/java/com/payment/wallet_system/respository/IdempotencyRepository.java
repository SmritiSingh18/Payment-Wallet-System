package com.payment.wallet_system.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.wallet_system.entity.IdempotencyRecord;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord,Long> {
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
