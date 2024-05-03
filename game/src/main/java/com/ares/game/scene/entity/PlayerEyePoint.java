package com.ares.game.scene.entity;

import com.ares.game.player.Player;
import com.ares.game.scene.Region;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class PlayerEyePoint extends Entity{
    private Player player;        // 所属的玩家
    private Region region;            // 关联的区域
    private Region relatedBigRegion;  // 关联的大圈区域（eye_point自身持有）

    public PlayerEyePoint() {
        setClientVisible(false);
    }

    @Override
    public ProtoScene.ProtEntityType getEntityType() {
        return ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT;
    }

    public int getRegionEntityId()
    {
        if (region == null)
        {
            log.debug("region is null");
            return 0;
        }

        return region.getEntityId();
    }
}
