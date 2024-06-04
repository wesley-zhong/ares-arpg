package com.ares.game.player.modules.basic;

import com.ares.core.exception.UnknownLogicException;
import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoInner;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class PlayerBasicModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerBasicModule.class);

    // 唯一ID类型
    public enum GuidType
    {
        GUID_NONE,
        GUID_AVATAR,    // 角色
        GUID_ITEM,      // 道具
    };

    private int level;
    private int exp;
    private String nickName;
    private long guidSeqId = 0; // guid 序列号记录 客户端把所有guid放一个map管理 所以服务器也只能用一个seq
    private boolean guidInit = false; // 是否已经初始化

    public PlayerBasicModule(Player player) {
        super(ProtoInner.GameModuleId.GMI_PlayerBasic, player);
    }

    @Override
    public void fromBin(BinServer.PlayerDataBin bin) {
        BinServer.PlayerBasicModuleBin moduleBin = bin.getBasicBin();
        level = moduleBin.getLevel();
        exp = moduleBin.getExp();
        nickName = moduleBin.getNickname();
        guidSeqId = moduleBin.getGuidSeqId();
        guidInit = true;
    }

    @Override
    public void toBin(BinServer.PlayerDataBin.Builder bin) {
        BinServer.PlayerBasicModuleBin.Builder builder = bin.getBasicBinBuilder();
        builder.setLevel(level);
        builder.setExp(exp);
        builder.setNickname(nickName);
        builder.setGuidSeqId(guidSeqId);
    }

    @Override
    public void onLogin(boolean isNewPlayer) {
        log.info("PlayerBasicModule onLogin. uid:{} module:{} isNewPlayer:{}", getPlayer().getUid(), getModuleId(), isNewPlayer);
    }

    public long genGuid(GuidType type) {
        if (!guidInit)
        {
            throw new UnknownLogicException("called before init, player:" + player);
        }
        guidSeqId++;
        if (guidSeqId > Integer.MAX_VALUE) // seq 使用过半的时候发出告警，等待后来人填坑
        {
            log.error("seq is big enough, seq:" + guidSeqId + ", type:" + type + ", player:" + player);
        }

        return (player.getUid() << 32) + guidSeqId;
    }
}
