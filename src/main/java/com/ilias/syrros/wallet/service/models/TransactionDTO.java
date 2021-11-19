package com.ilias.syrros.wallet.service.models;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Data
@Setter
@Getter
@Builder
public class TransactionDTO {

    private String globalId;
    private int typeId;
    private String transactionType;
    private BigDecimal amount;
    private long accountId;

    public TransactionDTO(){ }

    public TransactionDTO(String globalId, int typeId, String transactionType, BigDecimal amount, long accountId){
        this.globalId = globalId;
        this.typeId = typeId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.accountId = accountId;
    }
}
