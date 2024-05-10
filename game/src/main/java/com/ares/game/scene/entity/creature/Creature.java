package com.ares.game.scene.entity.creature;

import com.ares.game.scene.entity.Entity;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;

// 场景上的可战斗物件(带ability和属性)
@Getter
@Setter
public abstract class Creature extends Entity {
    private ProtoCommon.LifeState lifeState = ProtoCommon.LifeState.LIFE_NONE;
    private int configLevel = 0;     // 配表等级
    private int reviseLevel = 0;     // 修正等级
    private int level = 1;            // 最终等级

    public float getCurHp()
    {
//        return fight_prop_comp_.getCurHp();
        return getMaxHp();
    }

    public float getMaxHp()
    {
        // max hp 特殊处理
//        return fight_prop_comp_.getPropValue(FIGHT_PROP_MAX_HP, true);
        return 100;
    }

    public void setCurHp(float hp, boolean notify)
    {
    }

    // 设置满血前如果改变了属性，需要先计算一下属性
    public void setCurHpFull(boolean notify)
    {
        setCurHp(getMaxHp(), notify);
    }
}
