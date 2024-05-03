package com.ares.game.scene.entity;

import com.ares.game.player.Player;
import com.ares.game.scene.Region;
import com.ares.game.scene.Scene;
import com.game.protoGen.ProtoScene;

public class EntityMgr {
    // 创建场景实体
    public static SceneEntity createSceneEntity(Scene scene) {
        SceneEntity entity = new SceneEntity();
        entity.setEntityId(EntityUtils.LEVEL_RUNTIMEID);
        entity.setEntityName("Scene_" + scene.getSceneId());
        entity.setScene(scene);
        return entity;
    }

    // 创建eye_point实体
    public static PlayerEyePoint createPlayerEyePoint(Player player, Scene scene, Region region, Region relatedBigRegion)
    {
        PlayerEyePoint eyePoint = new PlayerEyePoint();
        int entityId = scene.genNewEntityId(ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT);
        eyePoint.setEntityId(entityId);
        eyePoint.setPosition(region.getPosition());
        eyePoint.setPlayer(player);
        eyePoint.setRegion(region);
        eyePoint.setRelatedBigRegion(relatedBigRegion);
        return eyePoint;
    }
}
