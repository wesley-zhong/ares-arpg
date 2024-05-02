package com.ares.game.player.modules.scene;

import com.ares.common.math.Vector3;
import com.ares.game.player.GamePlayer;
import com.ares.game.player.PlayerModule;
import com.ares.game.scene.Scene;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerSceneModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerSceneModule.class);

    // 需要持久化的变量
    private int myCurSceneId = 0;          // 在自己世界时的scene_id,用于db->login的恢复
    private int myPrevSceneId = 0;         // 之前自己的场景ID(主要用于记录联机前的场景)
    private PlayerWorld myPlayerWorld;    // 玩家自身拥有的大世界

    // 不需要持久化的变量
    Scene curScene;                // 玩家当前场景
    int curSceneId = 0;             // RT,避免主机销毁后客机不知道当前scene_id
    World curWorld;            // 玩家当前所在的大世界
    long curWorldOwnerUid = 0;      // 如名，主机大世界销毁仍然有效

    // 传送相关，无需序列化
    Scene destScene;                      // 传送目的场景
    Vector3 destPos;                      // 传送目的点
    Vector3 destRot;                      // 传送目的朝向
    ProtoScene.EnterType destEnterTYpe;     // 传送目的场景的方式
    ProtoScene.VisionType destVisionTYpe;   // 传送目的场景的出现方式

    public PlayerSceneModule(GamePlayer player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerScene, player);

        myPlayerWorld = new PlayerWorld();
        myPlayerWorld.setOwnPlayer(getPlayer());
    }

    @Override
    public void fromBin(BinServer.PlayerDataBin bin) {
        BinServer.PlayerSceneModuleBin moduleBin = bin.getSceneBin();
        myPlayerWorld.fromBin(moduleBin.getWorld());
        myCurSceneId = moduleBin.getMyCurSceneId();
        myPrevSceneId = moduleBin.getMyPrevSceneId();
    }

    @Override
    public void toBin(BinServer.PlayerDataBin.Builder bin) {
        BinServer.PlayerSceneModuleBin.Builder builder = bin.getSceneBinBuilder();
        myPlayerWorld.toBin(builder.getWorldBuilder());
        builder.setMyCurSceneId(myCurSceneId);
        builder.setMyPrevSceneId(myPrevSceneId);
    }

    @Override
    public void init() {
    //    myPlayerWorld.init();
    }

    @Override
    public void start() {
        myPlayerWorld.start();
    }
}
