package com.ares.game.scene.visitor;

import com.ares.game.scene.entity.Entity;
import com.game.protoGen.ProtoScene;

public class VisitAvatarVisitor extends Visitor {
    public VisitAvatarVisitor(Entity selfEntity) {
        super(selfEntity);
    }

    @Override
    public VisitorType getType() {
        return VisitorType.VISIT_AVATAR_VISITOR;
    }

    @Override
    protected boolean canAddEntity(Entity entity) {
        return entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR;
    }
}
