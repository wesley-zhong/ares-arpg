package com.ares.login.dal;

import com.ares.dal.mongo.CommonMongoDAO;
import com.ares.dal.mongo.MongoBaseDAO;
import com.ares.login.dal.DO.AccountDO;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDAO extends CommonMongoDAO<AccountDO> {
    public AccountDAO() {
        super(AccountDO.class);
    }
}
