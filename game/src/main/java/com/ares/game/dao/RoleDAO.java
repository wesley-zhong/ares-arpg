package com.ares.game.dao;

import com.ares.dal.mongo.MongoBaseDAO;
import com.ares.game.DO.RoleDO;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDAO extends MongoBaseDAO<RoleDO> {
    public RoleDAO() {
        super(RoleDO.class);
    }
}
