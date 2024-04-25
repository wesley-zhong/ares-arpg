package com.ares.game.player;

import com.ares.game.DO.RoleDO;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GamePlayer {
    private long uid;
    private long sceneId;

    private RoleDO roleDO;

    public GamePlayer(long id) {
        this.uid = id;
    }

    public long getUid() {
        return roleDO.getUid();
    }
}
