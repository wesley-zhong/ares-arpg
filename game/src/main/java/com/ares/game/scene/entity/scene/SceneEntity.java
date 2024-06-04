package com.ares.game.scene.entity.scene;

import com.ares.game.scene.entity.creature.Creature;
import com.game.protoGen.ProtoScene;

public class SceneEntity extends Creature {
    @Override
    public ProtoScene.ProtEntityType getEntityType() {
        return ProtoScene.ProtEntityType.PROT_ENTITY_SCENE;
    }
}
