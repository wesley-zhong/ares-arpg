package com.ares.game.player.modules.scene;

import com.ares.game.player.GamePlayer;
import com.ares.game.player.PlayerModule;
import com.ares.game.scene.world.PlayerWorld;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerSceneModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerSceneModule.class);

    // 需要持久化的变量
    private int myCurSceneId = 0;          // 在自己世界时的scene_id,用于db->login的恢复
    private int myCurPlayerSceneId = 0;   // 在自己大世界时的scene_id,用于db->login的恢复
    private int myPrevSceneId = 0;         // 之前自己的场景ID(主要用于记录联机前的场景)
    private PlayerWorld myPlayerWorld;    // 玩家自身拥有的大世界

    public PlayerSceneModule(GamePlayer player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerScene, player);
    }
}
