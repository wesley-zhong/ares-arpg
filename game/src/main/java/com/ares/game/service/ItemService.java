package com.ares.game.service;

import com.ares.game.player.Player;
import com.game.protoGen.ProtoItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ItemService {
    @Autowired
    PlayerRoleService playerRoleService;

    public ProtoItem.UseItemRes useItem(long uid, ProtoItem.UseItemReq req) {
        Player player = playerRoleService.getPlayer(uid);
        return player.getItemModule().useItem(req);
    }
}
