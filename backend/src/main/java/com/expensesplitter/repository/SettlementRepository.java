package com.expensesplitter.repository;

import com.expensesplitter.model.SettlementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementTransaction, Long> {
    List<SettlementTransaction> findByGroupId(Long groupId);
    List<SettlementTransaction> findByPayerIdOrReceiverIdOrderBySettledAtDesc(Long payerId, Long receiverId);
}
