package com.ares.dal.game;

import com.ares.common.bean.ServerType;
import com.ares.dal.DO.BaseDO;

public class UserOnlineStateDO extends BaseDO {
    private String tsrId;
    private String gsrId;


    public void setServerId(String serverId, int serverType) {
        if (serverType == ServerType.TEAM.getValue()) {
            tsrId = serverId;
            return;
        }
        if (serverType == ServerType.GAME.getValue()) {
            gsrId = serverId;
        }
    }

    public String getGsrId() {
        return gsrId;
    }

    public void setGsrId(String gsrId) {
        this.gsrId = gsrId;
    }


    public String getTsrId() {
        return tsrId;
    }

    public void setTsrId(String tsrId) {
        this.tsrId = tsrId;
    }


}
