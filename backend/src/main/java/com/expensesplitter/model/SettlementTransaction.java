package com.expensesplitter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt;

    private String note;
}
