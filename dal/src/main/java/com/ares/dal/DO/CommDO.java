package com.ares.dal.DO;

public class CommDO extends CASDO{
    public String  id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (target instanceof BaseDO baseDO) {
            return this.getId().equals(baseDO.getId());
        }
        return false;
    }
}
