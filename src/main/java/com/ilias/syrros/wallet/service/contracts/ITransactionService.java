package com.ilias.syrros.wallet.service.contracts;

import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.service.models.TransactionDTO;
import org.hibernate.StaleObjectStateException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ITransactionService {

    List<TransactionDTO> getTransactionsByAccountId(@NotNull long accountId, int pageNumber, int pageSize) throws AccountException;

    List<TransactionDTO> getTransactionsByAccountIdAndRangDates(@NotNull long accountId, LocalDateTime dateFrom, LocalDateTime dateTo, int pageNumber, int pageSize) throws AccountException;

    @Retryable( value = {OptimisticLockException.class, StaleObjectStateException.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    TransactionDTO createTransactionAndChangeBalance(String globalId, int typeId, BigDecimal amount, long accountId) throws AccountException;
}
