package com.ares.game.player.modules.item;

import com.game.protoGen.ProtoInner;

// 扣道具原因
public class SubItemReason {
    public final ProtoInner.ActionReasonType reasonType;

    public SubItemReason(final ProtoInner.ActionReasonType reasonType) {
        this.reasonType = reasonType;
    }
}
