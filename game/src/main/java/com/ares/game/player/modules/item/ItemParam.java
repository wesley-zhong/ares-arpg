package com.ares.game.player.modules.item;

import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;

public class ItemParam {
    public int itemId;
    public int count;
    public int level;

    public void fromBin(BinServer.ItemParamBin bin) {
        itemId = bin.getItemId();
        count = bin.getCount();
        level = bin.getLevel();
    }

    public void toBin(BinServer.ItemParamBin.Builder bin) {
        bin.setItemId(itemId);
        bin.setCount(count);
        bin.setLevel(level);
    }

    public void fromEquipParam(ProtoCommon.EquipParam proto) {
        itemId = proto.getItemId();
        count = proto.getItemNum();
        level = proto.getItemLevel();
    }

    public void toEquipParam(ProtoCommon.EquipParam.Builder proto) {
        proto.setItemId(itemId);
        proto.setItemNum(count);
        proto.setItemLevel(level);
    }

    public void toClient(ProtoCommon.ItemParam.Builder proto) {
        proto.setItemId(itemId);
        proto.setCount(count);
    }
}
