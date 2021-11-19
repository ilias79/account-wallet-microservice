package com.ilias.syrros.wallet.repository;

import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional(rollbackOn = AccountException.class)
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByOrderByIdAsc();

    Account findByUserId(long userId);

}
