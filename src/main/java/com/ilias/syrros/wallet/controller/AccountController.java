package com.ilias.syrros.wallet.controller;

import com.ilias.syrros.wallet.advice.RateLimiterException;
import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.aspect.RateLimit;
import com.ilias.syrros.wallet.converter.AccountDtoConverter;
import com.ilias.syrros.wallet.service.contracts.IAccountService;
import com.ilias.syrros.wallet.service.models.AccountDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class AccountController {

    Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Autowired
    private IAccountService accountService;
    private AccountDtoConverter accountDtoConverter = new AccountDtoConverter();

    @GetMapping("/account")
    @RateLimit(limit = 3, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find all accounts", notes = "Returns a collection of accounts")
    public ResponseEntity<?> get() throws RateLimiterException {

        logger.info("AccountController get method calls for getting all accounts");

        List<AccountDTO> accounts = accountService.findAll();

        if(!accounts.isEmpty() && accounts.size() > 0){
            return ResponseEntity.ok().body(
                    accounts
                    .stream()
                    .collect(Collectors.toList())
            );
        }
        else{
            throw new AccountException(404, "Account not found");
        }
    }

    @GetMapping("/account/{accountId}")
    @RateLimit(limit = 3, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find account of a given accountId", notes = "Returns a account by given accountId")
    public ResponseEntity<AccountDTO> getByAccountId(@PathVariable("accountId") long accountId) throws RateLimiterException{

        logger.info("accountController getByAccountId method calls for getting account from accountId");

        AccountDTO account = accountService.findById(accountId);

        if(account != null){
            return Optional.ofNullable(accountService.findById(accountId))
                    .map(w -> new ResponseEntity<>(
                            w,
                            HttpStatus.OK
                    )).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/{userId}/account")
    @RateLimit(limit = 3, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find account of a given userId", notes = "Returns a account by given userId")
    public ResponseEntity<?> getByUserId(@PathVariable("userId") long userId) throws RateLimiterException{

        logger.info("accountController getByUserId method calls for getting account from userId");

        AccountDTO account = accountService.findByUserId(userId);

        if(account != null){
            return Optional.ofNullable(accountService.findByUserId(userId))
                    .map(w -> new ResponseEntity<>(
                            w,
                            HttpStatus.OK
                    )).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/user/{userId}/account/create")
    @RateLimit(limit = 3, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Create account of a given user", notes = "Create a account and return created account")
    public ResponseEntity<?> create(@PathVariable("userId") long userId, @RequestBody double balance) throws RateLimiterException{

        logger.info("accountController create method calls for creating account");
        if (balance < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        AccountDTO createdAccount;

        try{
            createdAccount = accountService.createAccount(userId, BigDecimal.valueOf(balance));
            logger.info("accountController create method created account");
        }
        catch (AccountException exc){
            logger.error("accountController create method has an error");
            throw new AccountException(HttpStatus.UNPROCESSABLE_ENTITY.value(), exc.getDetail());

            //return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }
}
