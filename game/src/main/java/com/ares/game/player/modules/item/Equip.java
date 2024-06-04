package com.ares.game.player.modules.item;

import cfg.item.ItemType;
import com.ares.game.scene.entity.avatar.Avatar;
import lombok.Getter;
import lombok.Setter;

// Equip的两个特性，可穿戴、有属性的物品
@Getter
@Setter
public abstract class Equip extends Item {
    protected Avatar avatar;                  // 所属的角色
    protected boolean locked;

    public Equip(ItemType type, int itemId) {
        super(type, itemId);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int checkConsume() {
        return 0;
    }

    // 穿上装备时调用
    public void setOwner(Avatar avatar) {
        this.avatar = avatar;
    }

    // 脱下装备时调用
    public void resetOwner() {
        this.avatar = null;
    }

    // 获取所有者
    Avatar getOwner() {
        return avatar;
    }
}
