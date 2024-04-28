package com.ares.game.player.modules.basic;

import com.ares.game.player.GamePlayer;
import com.ares.game.player.PlayerModule;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerBasicModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerBasicModule.class);

    public PlayerBasicModule(GamePlayer player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerBasic, player);
    }

    public String getNickName(){
        return "nickname";
    }

    @Override
    public void onLogin(boolean isNewPlayer) {
        log.info("PlayerBasicModule onLogin. uid:{} module:{} isNewPlayer:{}", getPlayer().getUid(), getModuleId(), isNewPlayer);
    }
}
