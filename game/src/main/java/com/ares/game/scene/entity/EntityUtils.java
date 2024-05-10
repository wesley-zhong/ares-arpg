package com.ares.game.scene.entity;

import com.game.protoGen.ProtoScene;

public class EntityUtils {
    public static final int PEER_BITS = 3;
    public static final int CATEGORY_BITS = 5;
    public static final int IS_SYNCED_BITS = 1;
    public static final int SEQUENCE_BITS = 32 - (PEER_BITS + IS_SYNCED_BITS + CATEGORY_BITS);
    public static final int PEER_SHIFT = CATEGORY_BITS + IS_SYNCED_BITS + SEQUENCE_BITS;
    public static final int CATEGORY_SHIFT = IS_SYNCED_BITS + SEQUENCE_BITS;
    public static final int IS_SYNCED_SHIFT = SEQUENCE_BITS;
    public static final int EFFECT_CATE = 16;
    public static final int ATTACKUNIT_CATE = 17;
    public static final int CAMERA_CATE = 18;
    public static final int MANAGER_CATE = 19;
    public static final int LOCALGADGET_CATE = 20;
    public static final int LOCALMASSIVE_CATE = 21;
    public static final int LEVEL_RUNTIMEID = 0 << PEER_SHIFT | MANAGER_CATE << CATEGORY_SHIFT | 1 << IS_SYNCED_SHIFT | 1;

    // 获取实体ID
    public static int getEntityId(ProtoScene.ProtEntityType type, int index)
    {
        return type.getNumber() << 24 | index;
    }

    // 获取实体类型
    public static ProtoScene.ProtEntityType getEntityType(int entityId)
    {
        return ProtoScene.ProtEntityType.forNumber(entityId >> 24);
    }

    // 统一在一个地方 判断entity的类型
    // 是否是Gadget
    public static boolean isGadget(Entity Entity) {
        return Entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_GADGET;
    }
}
