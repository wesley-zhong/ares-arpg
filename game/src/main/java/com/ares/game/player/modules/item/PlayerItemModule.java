package com.ares.game.player.modules.item;

import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerItemModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerItemModule.class);

    public PlayerItemModule(Player player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerItem, player);
    }
}
