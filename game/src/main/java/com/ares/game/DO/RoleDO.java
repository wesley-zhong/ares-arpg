package com.ares.game.DO;


import com.ares.dal.DO.BaseDO;
import com.ares.dal.mongo.annotation.MdbName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MdbName("game")
public class RoleDO extends BaseDO {
    private long uid;
    private byte[] bin;
}
