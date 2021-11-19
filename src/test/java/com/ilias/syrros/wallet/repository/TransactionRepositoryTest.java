package com.ilias.syrros.wallet.repository;

import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.models.Account;
import com.ilias.syrros.wallet.models.Transaction;
import com.ilias.syrros.wallet.models.TransactionType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account account1;
    private Account account2;
    private Transaction transaction;

    @Before
    public void before(){

        account1 = new Account(1, BigDecimal.ZERO);
        account2 = new Account(2,BigDecimal.valueOf(10));
        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.flush();

        transaction = new Transaction(String.valueOf(UUID.randomUUID()), TransactionType.CREDIT, BigDecimal.valueOf(5), account1);
        //transaction.setId(100);
        entityManager.persist(transaction);
        entityManager.flush();
    }

    @Test
    public void testFindByAccount() {

        List<Transaction> transactions = transactionRepository.findByAccountId(account1.getId(), PageRequest.of(0,10));
        assertTrue(transactions.size() > 0);
        assertEquals(transactions.get(0).getAccount().getId(), account1.getId());
        assertEquals(transactions.get(0).getId(),transaction.getId());
    }

    @Test
    public void testSave_Credit() {

        Transaction transaction = new Transaction(String.valueOf(UUID.randomUUID()), TransactionType.DEBIT, BigDecimal.valueOf(20), account2);
        Transaction found = transactionRepository.save(transaction);
        assertNotNull(found);
        assertEquals(found.getAmount(),20);
        assertEquals(found.getTransactionType(), TransactionType.DEBIT);
    }

    @Test
    public void testSave_Debit() {

        Transaction transaction = new Transaction(String.valueOf(UUID.randomUUID()), TransactionType.DEBIT, BigDecimal.valueOf(10), account2);
        Transaction found = transactionRepository.save(transaction);
        assertNotNull(found);
        assertEquals(found.getAmount(),-10);
        assertEquals(found.getTransactionType(), TransactionType.DEBIT);
    }

    @Test
    public void testSave_NotUniqueGlobalId() {

        Transaction transaction = new Transaction(String.valueOf(-1), TransactionType.DEBIT, BigDecimal.valueOf(10), account2);
        try{
            transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(AccountException ex){
            assertEquals(ex.getCode(),400);
        }
    }

    @Test
    public void whenSave_NoBalance() {

        Transaction transaction = new Transaction(String.valueOf(UUID.randomUUID()), TransactionType.CREDIT, BigDecimal.valueOf(10), account2);
        try{
            Transaction found = transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(ConstraintViolationException ex){
            assertFalse(ex.getConstraintViolations().isEmpty());
            assertTrue(ex.getConstraintViolations().iterator().next().getMessage().contains("Transaction amount must be provided"));

        }
    }

    @Test
    public void whenSave_FailWrongAccount() {

        long accountId = 100;
        Account account = accountRepository.getOne(accountId);
        Transaction transaction = new Transaction(String.valueOf(UUID.randomUUID()), TransactionType.DEBIT, BigDecimal.valueOf(10), account);
        try{
            Transaction found = transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(DataIntegrityViolationException ex){
            assertTrue( ex.getMessage().contains("could not execute statement"));
        }
    }
}