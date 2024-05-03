package com.ares.game.player.modules.avatar;

import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.ares.game.scene.entity.avatar.Avatar;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class PlayerAvatarModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerAvatarModule.class);

    private Avatar curAvatar = null;

    public PlayerAvatarModule(Player player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerBasic, player);
    }

    public int getAvatarCount() {
        return 1;
    }

    public boolean isAllAvatarDead() {
        return false;
    }

    public boolean isRevivableAfterAllDead() {
        return true;
    }

    @Override
    public void fromBin(BinServer.PlayerDataBin bin) {
    }

    @Override
    public void toBin(BinServer.PlayerDataBin.Builder bin) {
    }
}
