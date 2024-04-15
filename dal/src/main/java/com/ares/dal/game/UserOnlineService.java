package com.ares.dal.game;


import com.ares.dal.redis.RedisDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserOnlineService {
    @Autowired
    private RedisDAO redisDAO;
    private static final String OLS = "ol:";

    public void saveUserOnlineDo(UserOnlineStateDO userOnlineStateDO) {
        String key = OLS + userOnlineStateDO.getId();
        redisDAO.set(key, userOnlineStateDO);
    }

    public UserOnlineStateDO getUserOnlineStateDO(long userId) {
        String key = OLS + userId;
        return redisDAO.get(key, UserOnlineStateDO.class);
    }
}
