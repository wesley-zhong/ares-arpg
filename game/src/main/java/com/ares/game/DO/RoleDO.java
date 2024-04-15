package com.ares.game.DO;


import com.ares.dal.DO.BaseDO;
import com.ares.dal.mongo.annotation.MdbName;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Getter
@Setter
@MdbName("game")
public class RoleDO extends BaseDO {
    private long uid;
    @BsonProperty("n")
    private String name;
    private int countTest;
}
