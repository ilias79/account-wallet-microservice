package com.ilias.syrros.wallet.service;

import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.converter.TransactionDtoConverter;
import com.ilias.syrros.wallet.models.Transaction;
import com.ilias.syrros.wallet.models.Account;
import com.ilias.syrros.wallet.models.TransactionType;
import com.ilias.syrros.wallet.repository.TransactionRepository;
import com.ilias.syrros.wallet.repository.TypeRepository;
import com.ilias.syrros.wallet.repository.AccountRepository;
import com.ilias.syrros.wallet.service.contracts.ITransactionService;
import com.ilias.syrros.wallet.service.models.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@PropertySource("classpath:application.properties")
public class TransactionService implements ITransactionService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    //@Autowired
    //private TypeRepository typeRepository;
    private TransactionDtoConverter transactionConverter = new TransactionDtoConverter();

    @Transactional(rollbackFor = AccountException.class)
    @Override
    public List<TransactionDTO> getTransactionsByAccountId(@NotNull long accountId, int pageNumber, int pageSize) throws AccountException {
        Pageable pages = PageRequest.of(pageNumber, pageSize);
        try {
            return  transactionRepository.findByAccountId(accountId, pages).stream().map(w -> transactionConverter.convert(w)).collect(Collectors.toList());
        } catch (Exception e){
            throw new AccountException(400, "AccountId does not exists - getTransactionsByAccountId method");
        }
    }

    @Transactional(rollbackFor = AccountException.class)
    @Override
    public List<TransactionDTO> getTransactionsByAccountIdAndRangDates(@NotNull long accountId, LocalDateTime dateFrom, LocalDateTime dateTo, int pageNumber, int pageSize) throws AccountException {
        Pageable pages = PageRequest.of(pageNumber, pageSize);
        try {
            return  transactionRepository.findByAccountIdAndLastUpdatedBetween(accountId, dateFrom, dateTo, pages).stream().map(w -> transactionConverter.convert(w)).collect(Collectors.toList());
        } catch (Exception e){
            throw new AccountException(400, "AccountId does not exists - getTransactionsByAccountId method");
        }
    }

    @Retryable( value = {OptimisticLockException.class, StaleObjectStateException.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = AccountException.class)
    @Override
    public TransactionDTO createTransactionAndChangeBalance(@NotBlank String globalId, @NotBlank int typeId, @NotNull BigDecimal amount, @NotNull long accountId) throws AccountException {
        log.info("Inside createTransactionAndChangeBalance method");
        try {
            if (globalId.startsWith("111")) {
                log.info(Thread.currentThread().getName() + " :: will sleep");
                Thread.sleep(10000);
            }
            if (typeId == 1) {
                accountService.decreaseAccountAmount(accountId, amount);
            }
            else if (typeId == 2) {
                accountService.increaseAccountAmount(accountId, amount);
            }
            Optional<Account> addedAccount = accountRepository.findById(accountId);

            if (addedAccount.isPresent()) {
                Transaction addTransaction = new Transaction(globalId, TransactionType.of(typeId), amount, addedAccount.get());
                Transaction createdTransaction = transactionRepository.save(addTransaction);
                if (createdTransaction != null) {
                    return transactionConverter.convert(createdTransaction);
                }
                else {
                    throw new AccountException(400, "Transaction couldn't be created. - createTransactionAndChangeBalance method");
                }
            }
            else {
                throw new AccountException(400, "Transaction couldn't be created. No suitable account exists - createTransactionAndChangeBalance method");
            }

        } catch(NumberFormatException | InterruptedException e){
            throw new AccountException(400, "Format Exception");
        }
    }
}
