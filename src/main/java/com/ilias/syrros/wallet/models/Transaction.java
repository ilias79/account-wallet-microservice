package com.ilias.syrros.wallet.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Entity
@Table(name="account_transaction")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @Column(name = "id")
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @NotNull
    @Column(name = "global_id", unique = true)
    private String globalId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @NotNull
    @Column(name = "amount")
    private BigDecimal amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "last_updated", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUpdated;

    public Transaction(){ }

    public Transaction( String globalId, TransactionType transType, BigDecimal amount, Account account) {
        this.globalId = globalId;
        this.transactionType = transType;
        this.amount = amount;
        this.account = account;
        this.lastUpdated = LocalDateTime.now();
    }
}
