package com.ares.login.dal.DO;

import com.ares.dal.DO.CASDO;
import com.ares.dal.DO.CommDO;
import com.ares.dal.mongo.MongoBaseDAO;
import com.ares.dal.mongo.annotation.MdbName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MdbName("account")
public class AccountDO extends CommDO {
    private long uid;
    private String channel; //渠道
}
