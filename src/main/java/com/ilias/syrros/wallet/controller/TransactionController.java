package com.ilias.syrros.wallet.controller;

import com.ilias.syrros.wallet.advice.ErrorResponse;
import com.ilias.syrros.wallet.advice.RateLimiterException;
import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.aspect.RateLimit;
import com.ilias.syrros.wallet.service.contracts.ITransactionService;
import com.ilias.syrros.wallet.service.contracts.IAccountService;
import com.ilias.syrros.wallet.service.models.TransactionDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class TransactionController {

    Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IAccountService accountService;

    @GetMapping("/accounts/{id}/transactions")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find all transactions by given accountId", notes = "Returns a collection of transactions by given accountId")
    public ResponseEntity<?> getByAccountId(@PathVariable("id") long id,
                                            @RequestParam(defaultValue = "0") Integer pageNumber,
                                            @RequestParam(defaultValue = "10") Integer pageSize) throws RateLimiterException {

        logger.info("TransactionController getByAccountId method calls for getting all transactions");

        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountId(id, pageNumber, pageSize);

        if(!transactions.isEmpty() && transactions.size() > 0){
            return ResponseEntity.ok().body(
                        transactions
                        .stream()
                        .collect(Collectors.toList())
            );
        }
        else
            throw new AccountException(404, "Transactions not found");
    }

    @GetMapping("/accounts/{id}/transactions/dates")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find all transactions by given  in date ranges", notes = "Returns a collection of transactions by given accountId and range dates")
    public ResponseEntity<?> getByAccountIdAndRangeDates(@PathVariable("id") long id,
                                                         @RequestParam("dateFrom")
                                                         @DateTimeFormat(pattern = "dd.MM.yyyy") Date dateFrom,
                                                         @RequestParam("dateTo")
                                                         @DateTimeFormat(pattern = "dd.MM.yyyy") Date dateTo,
                                            @RequestParam(defaultValue = "0") Integer pageNumber,
                                            @RequestParam(defaultValue = "10") Integer pageSize) throws RateLimiterException {

        logger.info("TransactionController getByAccountId method calls for getting all transactions");
        LocalDateTime from = LocalDateTime.ofInstant(dateFrom.toInstant(), ZoneId.systemDefault());
        LocalDateTime to = LocalDateTime.ofInstant(dateTo.toInstant(), ZoneId.systemDefault());
        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountIdAndRangDates(id, from, to, pageNumber, pageSize);

        if(!transactions.isEmpty() && transactions.size() > 0){
            return ResponseEntity.ok().body(
                    transactions
                            .stream()
                            .collect(Collectors.toList())
            );
        }
        else
            throw new AccountException(404, "Transactions not found");
    }

    @PostMapping("/transaction")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Add transaction of a given transaction and add balance", notes = "Create transaction and add balance")
    public ResponseEntity<?> creditOrDebit(@RequestBody TransactionDTO transaction) throws RateLimiterException{

        logger.info("TransactionController add method is calling");

        int typeId = transaction.getTypeId();
        BigDecimal amount = transaction.getAmount();
        long accountId = transaction.getAccountId();

        if (typeId != 1 && typeId != 2)
            return new ResponseEntity<>(new ErrorResponse("TypeId should be debit or credit", 400), HttpStatus.BAD_REQUEST);
        if (amount.signum() == -1)
            return new ResponseEntity<>(new ErrorResponse("Balance should not be negative", 400), HttpStatus.BAD_REQUEST);
        if (typeId == 1 && accountService.findById(transaction.getAccountId()).getBalance().compareTo(transaction.getAmount()) == -1)
            return new ResponseEntity<>(new ErrorResponse("There is no enough balance", 400), HttpStatus.BAD_REQUEST);

        TransactionDTO createdTransaction;
        try{
            createdTransaction = transactionService.createTransactionAndChangeBalance(transaction.getGlobalId(),
                                                                                      typeId,
                                                                                      amount,
                                                                                      accountId);

            logger.info("TransactionController created transaction = " + createdTransaction.getGlobalId());
        }
        catch (AccountException exc){
            logger.error("TransactionController creditOrDebit method has an error");
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
