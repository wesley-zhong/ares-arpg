package com.ares.game.scene.world;

import com.ares.core.excetion.UnknownLogicException;
import com.ares.game.player.GamePlayer;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneMgr;
import com.ares.game.scene.SceneUtil;
import com.ares.game.scene.subclass.PlayerWorldScene;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  world就是逻辑层的大世界，包含一个mainScene，多个房间场景
 *  维护大世界的所有Scene数据, 以及当前所有玩家的状态数据
 **/
public class PlayerWorld extends World{
    private static final Logger log = LoggerFactory.getLogger(PlayerWorld.class);

    private int level = 0;                // 当前大世界等级,初始等级为0
    private GamePlayer ownPlayer;

    public PlayerWorld(int worldId){
        super(worldId);
    }

    public GamePlayer getOwnPlayer() {
        return ownPlayer;
    }
    public void setOwnPlayer(GamePlayer player) {
        this.ownPlayer = player;
        if (player != null) {
            setOwnerUid(player.getUid());
        }
        else {
            setOwnerUid(0);
        }
    }

    public static int getDefaultWorldId() {
        return 1;
    }

    @Override
    public ProtoCommon.WorldType getWorldType() {
        return ProtoCommon.WorldType.WORLD_PLAYER;
    }

    @Override
    public void init() {
        super.init();
        long uid = getOwnerUid();

        for (Scene scene : sceneMap.values()) {
            scene.init();
        }

        // 如果从未创建过大世界，在初始化的时候创建
        int mainSceneId = getDefaultWorldId();
        if (!sceneMap.containsKey(mainSceneId)) {
            Scene mainScene = SceneMgr.createScene(mainSceneId, ownPlayer);
            if (mainScene == null)
            {
                throw new UnknownLogicException("Can't create  main scene. " + mainSceneId + " uid: " + uid) ;
            }
            if (!(mainScene instanceof PlayerWorldScene))
            {
                throw new UnknownLogicException("mainScene is not PlayerWorldScene "+ mainSceneId);
            }
            mainScene.init();
            sceneMap.put(mainSceneId, mainScene);
        }
    }

    @Override
    public Scene createScene(int sceneId) {
        return createPersonalScene(sceneId);
    }

    // 创建玩家的个人场景(支持PlayerWorldScene/PlayerRoomScene)
    private Scene createPersonalScene(int sceneId)
    {
        GamePlayer player = getOwnPlayer();
        if (player == null)
        {
            throw new UnknownLogicException("getOwnPlayer fails");
        }
        if (!SceneUtil.isPlayerScene(ProtoCommon.SceneType.SCENE_WORLD))
        {
            throw new UnknownLogicException("scene is not PlayerScene: " + sceneId);
        }
        long uid = player.getUid();
        Scene scene = SceneMgr.createScene(sceneId, player);
        if (scene == null)
        {
            log.warn("scene is null, sceneId: " + sceneId + " uid: " + uid);
            return null;
        }
        scene.init();
        scene.start();
        if (sceneMap.put(sceneId, scene) != null)
        {
            log.warn("scene already exist, sceneId:" + sceneId);
            return null;
        }
        return scene;
    }

    @Override
    public PlayerWorldScene getMainWorldScene() {
        return (PlayerWorldScene) super.getMainWorldScene();
    }

    @Override
    public boolean isWorldFull() {
        return getPlayerCount() >= 4;
    }

    @Override
    public int checkKickPlayer(long targetUid) {
        return 0;
    }

    @Override
    public void kickPlayer(long uid) {

    }

    @Override
    public void kickAllPlayer(int reason, boolean canKickOwner) {

    }
}
