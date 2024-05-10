package com.ares.dal.game;

import com.ares.common.bean.ServerType;
import com.ares.dal.redis.RedisCasDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOnlineStateDO extends RedisCasDO {
    private String tmSrId;
    private String gmSrId;
    private long targetId;
}
