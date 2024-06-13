package com.ares.game.gm;

import cfg.item.ItemLimitType;
import com.ares.common.excelconfig.ExcelConfigMgr;
import com.ares.game.player.Player;
import com.ares.game.player.modules.item.ActionReason;
import com.ares.game.player.modules.item.ItemParam;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;

import java.util.ArrayList;
import java.util.List;

public class AddItem implements AbstractGMAction{
    public static final int MagicNumAllItem = 0;

    @Override
    public void doAction(Player player, ProtoGame.DebugCmdReq reqMsg, ProtoGame.DebugCmdRes.Builder response) {
        if (Integer.parseInt(reqMsg.getParam1()) == MagicNumAllItem) {
            List<ItemParam> itemParamList = new ArrayList<>();
            int count = Integer.parseInt(reqMsg.getParam2());
            for (cfg.Item itemConf : ExcelConfigMgr.getTables().getTbItem().getDataList()) {
                if (itemConf == null) {
                    continue;
                }
                ItemParam itemParam = new ItemParam();
                itemParam.itemId = itemConf.id;
                itemParam.count = count;
                itemParamList.add(itemParam);
            }
            player.getItemModule().addItemByParamBatch(itemParamList, new ActionReason(ProtoInner.ActionReasonType.ACTION_REASON_GM, ItemLimitType.GM));
        } else {
            ItemParam itemParam = new ItemParam();
            itemParam.itemId = Integer.parseInt(reqMsg.getParam1());
            itemParam.count = Integer.parseInt(reqMsg.getParam2());
            player.getItemModule().addItemByParamBatch(List.of(itemParam), new ActionReason(ProtoInner.ActionReasonType.ACTION_REASON_GM, ItemLimitType.GM));
        }
    }
}
