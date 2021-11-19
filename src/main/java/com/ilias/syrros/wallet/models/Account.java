package com.ilias.syrros.wallet.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
@Entity
@Table(name = "account")
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Column(name = "version_num")
    private Long version;

    @NotNull
    @Column(name = "user_id")
    private long userId;

    @Column(name = "account_num")
    private String accountNumber;

    @NotNull
    @Min(0)
    @Column(name = "balance",nullable = false)
    private BigDecimal balance;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    public Account(){ }

    public Account(long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
        this.lastUpdated = new Date();
    }
}
