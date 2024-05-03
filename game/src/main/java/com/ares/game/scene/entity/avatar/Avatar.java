package com.ares.game.scene.entity.avatar;

import com.ares.common.math.Vector3;
import com.ares.game.player.Player;
import com.ares.game.scene.entity.creature.Creature;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;

// 角色
@Getter
@Setter
public class Avatar extends Creature {
    private Player player;      // 所属的玩家
    private Vector3 prevSpeed;        // 上一次收到的客户端速度
    private Vector3 lastValidPos;    // 最后一次处于正常状态(站、走、跑)的坐标
    private Vector3 lastValidRot;    // 最后一次处于正常状态(站、走、跑)的朝向

    @Override
    public ProtoScene.ProtEntityType getEntityType() {
        return ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR;
    }
    // 清除速度
    public void clearSpeed()
    {
        setSpeed(Vector3.ZERO);
        setPrevSpeed(Vector3.ZERO);
    }

    public void clearMotionState() {
        setMotionState(ProtoScene.MotionState.MOTION_STANDBY);
    }

    @Override
    public boolean canEnterRegion() {
        return true;
    }
}
