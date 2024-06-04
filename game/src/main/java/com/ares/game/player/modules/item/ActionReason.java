package com.ares.game.player.modules.item;

import cfg.item.ItemLimitType;
import com.game.protoGen.ProtoInner;

// 获得道具的原因和产出来源类型
public class ActionReason {
    public final ProtoInner.ActionReasonType reasonType;
    public final ItemLimitType limitType;

    public ActionReason(final ProtoInner.ActionReasonType reasonType, final ItemLimitType limitType) {
        this.reasonType = reasonType;
        this.limitType = limitType;
    }
}
