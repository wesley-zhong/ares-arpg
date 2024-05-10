package com.ares.game.scene.visitor;

import com.ares.game.scene.entity.Entity;
import com.game.protoGen.ProtoScene;

/**
 * 查找附近的entity
 * selfEntity一般是avatar, 用于avatar在场景中移动（包括加入，离开场景）
 **/
public class VisitEntityVisitor extends Visitor {
    public VisitEntityVisitor(Entity selfEntity) {
        super(selfEntity);
    }

    @Override
    public VisitorType getType() {
        return VisitorType.VISIT_ENTITY_VISITOR;
    }

    @Override
    protected boolean canAddEntity(Entity entity) {
        ProtoScene.ProtEntityType entityType = entity.getEntityType();
        switch (entityType)
        {
            case PROT_ENTITY_MONSTER:
            case PROT_ENTITY_NPC:
            case PROT_ENTITY_GADGET:
            case PROT_ENTITY_WEATHER:
                return true;
            case PROT_ENTITY_AVATAR:
            {
                if (selfEntity == null)
                {
                    return false;
                }
                ProtoScene.ProtEntityType selfEntityType = selfEntity.getEntityType();
                if (selfEntityType == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
                {
                    return !entity.equals(selfEntity);
                }
                else if (selfEntityType == ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT)
                {
                    return entity.getPlayer() != selfEntity.getPlayer();
                }
                return true;
            }
            case PROT_ENTITY_EYE_POINT:
            {
                if (selfEntity == null)
                {
                    return false;
                }
                ProtoScene.ProtEntityType selfEntityType = selfEntity.getEntityType();
                if (selfEntityType == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
                {
                    return entity.getPlayer() != selfEntity.getPlayer();
                }
                else if (selfEntityType == ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT)
                {
                    return !entity.equals(selfEntity);
                }
                return true;
            }
            default:
                return false;
        }

    }
}
