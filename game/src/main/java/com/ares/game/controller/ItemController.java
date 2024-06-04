package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.game.service.ItemService;
import com.game.protoGen.ProtoItem;
import com.game.protoGen.ProtoMsgId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemController {
    @Autowired
    ItemService itemService;

    @MsgId(ProtoMsgId.MsgId.USE_ITEM_REQ_VALUE)
    public ProtoItem.UseItemRes useItem(long uid, ProtoItem.UseItemReq req) {
        return itemService.useItem(uid, req);
    }
}
