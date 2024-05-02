package com.ares.game.player.modules.basic;

import com.ares.game.player.GamePlayer;
import com.ares.game.player.PlayerModule;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class PlayerBasicModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerBasicModule.class);

    private int level;
    private int exp;
    private String nickName;

    public PlayerBasicModule(GamePlayer player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerBasic, player);
    }

    @Override
    public void fromBin(BinServer.PlayerDataBin bin) {
        BinServer.PlayerBasicModuleBin moduleBin = bin.getBasicBin();
        level = moduleBin.getLevel();
        exp = moduleBin.getExp();
        nickName = moduleBin.getNickname();;
    }

    @Override
    public void toBin(BinServer.PlayerDataBin.Builder bin) {
        BinServer.PlayerBasicModuleBin.Builder builder = bin.getBasicBinBuilder();
        builder.setLevel(level);
        builder.setExp(exp);
        builder.setNickname(nickName);
    }

    @Override
    public void onLogin(boolean isNewPlayer) {
        log.info("PlayerBasicModule onLogin. uid:{} module:{} isNewPlayer:{}", getPlayer().getUid(), getModuleId(), isNewPlayer);
    }
}
