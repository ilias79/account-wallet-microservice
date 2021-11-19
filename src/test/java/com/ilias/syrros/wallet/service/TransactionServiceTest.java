package com.ilias.syrros.wallet.service;

import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.converter.AccountDtoConverter;
import com.ilias.syrros.wallet.models.Account;
import com.ilias.syrros.wallet.models.Transaction;
import com.ilias.syrros.wallet.models.TransactionType;
import com.ilias.syrros.wallet.repository.TransactionRepository;
import com.ilias.syrros.wallet.repository.AccountRepository;
import com.ilias.syrros.wallet.service.models.TransactionDTO;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    @TestConfiguration
    static class TransactionServiceTestContextConfiguration {

        @Bean
        public TransactionService transactionService() {
            return new TransactionService();
        }
        @Bean
        public MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }

    static int globalIdCounter = 1;
    @Autowired
    private TransactionService transactionService;
    private AccountDtoConverter accountDtoConverter = new AccountDtoConverter();
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private AccountService accountService;

    private Account account1;
    private Account account2;
    private Transaction transactionCredit;
    private Transaction transactionDebit;

    @Before
    public void setUp() throws AccountException {

        account1 = new Account(1, BigDecimal.ZERO);
        account2 = new Account(2, BigDecimal.valueOf(10));
        //account1.setId(1);
        //account2.setId(2);
        transactionCredit = new Transaction(String.valueOf(globalIdCounter++), TransactionType.DEBIT, BigDecimal.valueOf(20), account1);
        //transactionCredit.setId(5);
        transactionDebit = new Transaction(String.valueOf(globalIdCounter++), TransactionType.CREDIT, BigDecimal.valueOf(10), account2);
        //transactionDebit.setId(6);
        createMockito();
    }

    @Test
    public void testGetTransactionsByAccountId_Success() throws AccountException {
        List<TransactionDTO> found = transactionService.getTransactionsByAccountId(account1.getId(), 0, 10);
        assertNotNull(found);
        assertTrue(found.size() == 1);
    }

    @Test
    public void testCreateTransaction_DebitFailure() throws AccountException {

        BigDecimal amount = BigDecimal.valueOf(100);
        int counter = globalIdCounter++;
        Mockito.when(accountService.decreaseAccountAmount(account2.getId(),amount)).
                thenThrow(new AccountException());
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
    }
    @Test
    @Ignore
    public void testCreateTransaction_SuccessCredit() throws AccountException {

        BigDecimal amount = BigDecimal.valueOf(100);
        Mockito.when(accountService.increaseAccountAmount(account1.getId(),amount)).thenReturn(accountDtoConverter.convert(account1));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = globalIdCounter++;
        TransactionDTO found = transactionService.createTransactionAndChangeBalance(String.valueOf(counter),2,amount, account1.getId());
        assertNotNull(found);
    }

    private void createMockito(){

        Mockito.when(accountService.findById(account1.getId())).thenReturn(accountDtoConverter.convert(account1));
        Mockito.when(transactionRepository.findByAccountId(account1.getId(), PageRequest.of(0,10))).thenReturn(Arrays.asList(transactionCredit));
        Mockito.when(transactionRepository.findByAccountId(account2.getId(), PageRequest.of(0, 10))).thenReturn(Arrays.asList(transactionCredit));
        Mockito.when(accountService.findById(account1.getId())).thenReturn(accountDtoConverter.convert(account1));
        Mockito.when(accountService.findById(account2.getId())).thenReturn(accountDtoConverter.convert(account2));
    }
}
