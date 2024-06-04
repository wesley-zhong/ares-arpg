package com.ares.game.player.modules.item;

import cfg.item.ItemType;
import com.ares.common.excelconfig.ExcelConfigMgr;
import com.ares.core.exception.UnknownLogicException;
import com.ares.game.player.Player;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoItem;
import com.game.protoGen.ProtoMsgId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Item {
    private final ItemType itemType; // 道具类型
    private final int itemId; // 配置ID
    private long guid;  // 唯一ID
    private Player player;    // 所属玩家

    public Item(ItemType itemType, int itemId) {
        this.itemType = itemType;
        this.itemId = itemId;
   }

    public void fromBin(BinServer.ItemBin bin) {
        guid = bin.getGuid();
    }

    public void toBin(BinServer.ItemBin.Builder bin) {
        bin.setItemType(itemType.getNumber());
        bin.setItemId(itemId);
        bin.setGuid(guid);
    }

    public void toClient(ProtoCommon.Item.Builder pb){
        pb.setItemId(itemId);
        pb.setGuid(guid);
    }

    public void init(boolean isFirstCreate) {
        cfg.item.Item itemConfig = getItemConfig();
        if (itemConfig == null)
        {
            throw new UnknownLogicException("getItemConfig failed, itemId:" + itemId);
        }

        if (guid == 0)
        {
            throw new UnknownLogicException("item guid is 0, itemId:" + itemId);
        }

        if (itemType.getNumber() != itemConfig.type)
        {
            throw new UnknownLogicException("item_type mismatch, itemId:" + itemId
                    + " bin_type:" + itemType.getNumber() + " config_type:" + itemConfig.type);
        }

        if (itemType != getItemType())
        {
            throw new UnknownLogicException("item_type mismatch, itemId:" + itemId
                    + " bin_type:" + itemType.getNumber() + " mem_type:" + getItemType().getNumber());
        }
    }

    public abstract int getItemCount();

    public abstract ItemType getItemType();

    public ItemParam toItemParam() {
        ItemParam param = new ItemParam();
        param.itemId = itemId;
        param.count = getItemCount();
        return param;
    }

    // 检查是否可以被消耗
    public abstract int checkConsume();

    public cfg.item.Item getItemConfig() {
        return ExcelConfigMgr.getTables().getTbItem().get(itemId);
    }

    public void setItemOwner(Player player) {
        this.player = player;
    }

    public void resetItemOwner() {
        this.player = null;
    }

    public Player getItemOwner() {
        return player;
    }

    public void notifyItemChange(){
        if (player != null)
        {
            ProtoItem.StoreItemChangeNtf.Builder builder = ProtoItem.StoreItemChangeNtf.newBuilder();
            builder.setStoreType(player.getItemModule().getItemStoreType(getGuid()));
            toClient(builder.addItemListBuilder());
            player.sendMessage(ProtoMsgId.MsgId.STORE_ITEM_CHANGE_NTF, builder.build());
        }
    }
}
