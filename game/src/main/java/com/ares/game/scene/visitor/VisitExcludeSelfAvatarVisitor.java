package com.ares.game.scene.visitor;

import com.ares.game.scene.entity.Entity;
import com.game.protoGen.ProtoScene;

public class VisitExcludeSelfAvatarVisitor extends Visitor {
    public VisitExcludeSelfAvatarVisitor(Entity selfEntity) {
        super(selfEntity);
    }

    @Override
    public VisitorType getType() {
        return VisitorType.VISIT_EXCLUDE_SELF_AVATAR_VISITOR;
    }

    @Override
    protected boolean canAddEntity(Entity entity) {
        return entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR
                && selfEntity != null
                && !selfEntity.equals(entity);
    }
}