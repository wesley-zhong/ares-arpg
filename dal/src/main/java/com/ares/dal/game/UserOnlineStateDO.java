package com.ares.dal.game;

import com.ares.common.bean.ServerType;
import com.ares.dal.redis.RedisCasDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOnlineStateDO extends RedisCasDO {
    private String tsrId;
    private String gmSrId;
    private String gtSrId;

    public void setServerId(String serverId, int serverType) {
        if (serverType == ServerType.TEAM.getValue()) {
            tsrId = serverId;
            return;
        }
        if (serverType == ServerType.GAME.getValue()) {
            gmSrId = serverId;
        }
        if (serverType == ServerType.GATEWAY.getValue()) {
            gtSrId = serverId;
        }
    }
}
