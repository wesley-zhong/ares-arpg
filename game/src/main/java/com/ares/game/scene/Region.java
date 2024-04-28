package com.ares.game.scene;

import com.ares.game.scene.entity.Entity;
import com.game.protoGen.ProtoScene;

public class Region extends Entity {
    public Region() {}

    @Override
    public ProtoScene.ProtEntityType getEntityType() {
        return ProtoScene.ProtEntityType.PROT_ENTITY_REGION;
    }
}
