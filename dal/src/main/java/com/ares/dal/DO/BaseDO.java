package com.ares.dal.DO;

/**
 * note: if we use common dao to save DO we should use  DO extends BaseDO
 *
 * @author zhongwq
 */

public class BaseDO  extends CASDO {
    public long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return  Long.hashCode(id);
    }

    @Override
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (target instanceof BaseDO baseDO) {
            return this.getId() == baseDO.getId();
        }
        return false;
    }
}
