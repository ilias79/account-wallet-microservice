package com.ilias.syrros.wallet.repository;

import com.ilias.syrros.wallet.advice.AccountException;
import com.ilias.syrros.wallet.models.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;

@Repository
@Transactional(rollbackOn = AccountException.class)
public interface TypeRepository extends JpaRepository<Type, Integer> {

}
