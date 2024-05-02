package com.ares.dal.game;

import com.ares.dal.redis.RedisDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserTokenService {
    @Autowired
    private RedisDAO redisDAO;
    private static String TOKE = "t:";

    public boolean checkToken(long uid, String token) {
        String key = TOKE + uid;
        String existToken = redisDAO.get(key);
        if (existToken == null) {
            return false;
        }
        return existToken.equals(token);
    }

    public void saveToken(long uid, String token) {
        String key = TOKE + uid;
        redisDAO.set(key, token);
    }
}
