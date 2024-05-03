package com.ares.game.player.modules.scene;

import com.ares.common.math.Vector3;
import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.ares.game.scene.Scene;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class PlayerSceneModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerSceneModule.class);

    public enum EnterSceneState
    {
        ENTER_SCENE_NONE,   // 尚未进入场景
        ENTER_SCENE_NOTIFY, // 服务器通知
        ENTER_SCENE_READY,  // 客户端READY
        ENTER_SCENE_INIT,   // 客户端场景初始化完毕
        ENTER_SCENE_DONE,   // 进场流程结束done
        ENTER_SCENE_POST,   // 进场流程结束post
    };

    // 需要持久化的变量
    private int myCurSceneId = 0;          // 在自己世界时的scene_id,用于db->login的恢复
    private int myPrevSceneId = 0;         // 之前自己的场景ID(主要用于记录联机前的场景)
    private PlayerWorld myPlayerWorld;    // 玩家自身拥有的大世界

    // 不需要持久化的变量
    private Scene curScene;                // 玩家当前场景
    private int curSceneId = 0;             // RT,避免主机销毁后客机不知道当前scene_id
    private World curWorld;            // 玩家当前所在的大世界
    private long curWorldOwnerUid = 0;      // 如名，主机大世界销毁仍然有效

    // 传送相关，无需序列化
    private EnterSceneState enterSceneState = EnterSceneState.ENTER_SCENE_NONE;
    private boolean inTransfer;
    private Scene destScene;                      // 传送目的场景
    private Vector3 destPos;                     // 传送目的位置
    private Vector3 destRot;                     // 传送目的朝向
    private ProtoScene.EnterType destEnterType;     // 传送目的场景的方式
    private ProtoScene.VisionType destVisionType;   // 传送目的场景的出现方式
    private boolean enterReLogin;
    private boolean clientReLogin;
    private int enterSceneToken;

    public PlayerSceneModule(Player player) {
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

    public int joinPlayerScene(long targetUid) {
        return 0;
    }

    public PlayerWorld getCurPlayerWorld() {
        if (curWorld != null && curWorld instanceof PlayerWorld) {
            return (PlayerWorld) curWorld;
        }
        return null;
    }

    // 设置当前的场景
    public void setCurScene(Scene targetScene)
    {
        if (curScene == targetScene)
        {
            return;
        }
        if (curScene != null)
        {
//            cur_scene_ptr->erasePlayerViewMgr(getPlayer().getUid());
//            destroyEntityWhenLeaveScene();
        }

        curScene = targetScene;
        if (curScene != null)
        {
//            scene_ptr->insertPlayerViewMgr(getPlayer());
            curSceneId = curScene.getSceneId();
        }
        else
        {
            curSceneId = 0;
        }
    }

    // 获得玩家的PeerID
    public int getPeerId()
    {
        Scene curScene1 = getCurScene();
        if (curScene1 == null)
        {
            return 0;
        }

        return curScene1.getPeerId(player.getUid());
    }

    public void playerLeaveScene() {
        player.onLeaveScene();
        setCurScene(null);
    }
}
