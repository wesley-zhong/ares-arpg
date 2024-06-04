package com.ares.game.player.modules.item;

import com.ares.game.player.Player;
import com.game.protoGen.ProtoCommon;

public class PackItemStore extends ItemStore {
    public PackItemStore(Player player) {
        super(player);
    }

    @Override
    public ProtoCommon.StoreType getStoreType() {
        return ProtoCommon.StoreType.STORE_PACK;
    }
}
