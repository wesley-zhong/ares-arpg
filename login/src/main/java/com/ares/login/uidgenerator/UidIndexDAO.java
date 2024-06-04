package com.ares.login.uidgenerator;

import com.ares.dal.redis.RedisDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UidIndexDAO {
    @Autowired
    private RedisDAO redisDAO;

    private static String key = "uid_index";

    public int getCurrentIndex(){
        String value = redisDAO.get(key);
        if (value != null) {
            return Integer.parseInt(value);
        }
        else {
            return 0;
        }
    }

    public int getNextIndex(){
        return (int) redisDAO.incr(key);
    }
}
