package com.ares.game.player.modules.item;

import cfg.item.ItemType;

public class Weapon extends Equip {
    public Weapon(int itemId) {
        super(ItemType.WEAPON, itemId);
    }

    @Override
    public ItemType getItemType() {
        return ItemType.WEAPON;
    }
}
