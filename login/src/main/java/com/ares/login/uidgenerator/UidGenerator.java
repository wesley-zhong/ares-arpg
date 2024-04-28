package com.ares.login.uidgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UidGenerator {
    @Autowired
    private UidIndexDAO uidIndexDAO;

    private UidPool uidPool = new UidPool();

    public int getCurrentIndex(){
        return uidIndexDAO.getCurrentIndex();
    }

    public int getNextId(List<Integer> prefixRange){
        return uidPool.getUid(uidIndexDAO.getNextIndex(), prefixRange);
    }
}
