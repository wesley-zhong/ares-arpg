package com.ares.game.scene.entity.monster;

import com.ares.game.scene.entity.EntityMgr;
import com.ares.game.scene.entity.creature.Creature;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Monster extends Creature {
    private final int monsterId;

    public Monster(final int monsterId) {
        this.monsterId = monsterId;
    }

    @Override
    public ProtoScene.ProtEntityType getEntityType() {
        return ProtoScene.ProtEntityType.PROT_ENTITY_MONSTER;
    }

    public void initWithParam(EntityMgr.MonsterParam monsterParam) {
        setConfigLevel(monsterParam.getConfigLevel());
        setReviseLevel(monsterParam.getReviseLevel());
        setLevel(monsterParam.getLevel());
        setBornPosition(monsterParam.getPos());
        setRotation(monsterParam.getRot());
        setBornPos(monsterParam.getPos());
        setBornRot(monsterParam.getRot());
        setConfigId(monsterParam.getConfigId());
    }

    public void init(){

    }
}
