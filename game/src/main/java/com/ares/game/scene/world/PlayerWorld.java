package com.ares.game.scene.world;

import com.ares.core.excetion.UnknownLogicException;
import com.ares.core.utils.TimeUtils;
import com.ares.game.player.Player;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneMgr;
import com.ares.game.scene.SceneUtil;
import com.ares.game.scene.subclass.PlayerWorldScene;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *  world就是逻辑层的大世界，包含一个mainScene，多个房间场景
 *  维护大世界的所有Scene数据, 以及当前所有玩家的状态数据
 **/
@Getter
@Setter
public class PlayerWorld extends World{
    private static final Logger log = LoggerFactory.getLogger(PlayerWorld.class);

    public static int MAX_PLAYER_COUNT = 4;

    private int level = 0;                // 当前大世界等级,初始等级为0
    private Player ownPlayer;

    public PlayerWorld(){
    }

    public Player getOwnPlayer() {
        return ownPlayer;
    }

    public void setOwnPlayer(Player player) {
        this.ownPlayer = player;
        if (player != null) {
            setOwnerUid(player.getUid());
        }
        else {
            setOwnerUid(0);
        }
    }

    public static int getDefaultWorldMainSceneId() {
        return 1;
    }

    @Override
    public ProtoCommon.WorldType getWorldType() {
        return ProtoCommon.WorldType.WORLD_PLAYER;
    }

    public void fromBin(BinServer.WorldBin bin) {
        level = bin.getLevel();
        long uid = getOwnerUid();
        for (BinServer.SceneBin sceneBin : bin.getSceneListList()) {
            int sceneId = sceneBin.getSceneId();
            Scene scene = SceneMgr.createScene(sceneId, ownPlayer);
            scene.fromBin(sceneBin);
            if (sceneMap.put(sceneId, scene) != null) {
                throw new UnknownLogicException("scene already exist, uid:" + uid + " scene_id:" + sceneId);
            }
        }
    }

    public void toBin(BinServer.WorldBin.Builder bin) {
        bin.setLevel(level);
        for (Map.Entry<Integer, Scene> entry : sceneMap.entrySet()) {
            int sceneId = entry.getKey();
            Scene scene = entry.getValue();
            BinServer.SceneBin.Builder sceneBinBuilder = BinServer.SceneBin.newBuilder();
            scene.toBin(sceneBinBuilder);
            bin.addSceneList(sceneBinBuilder);
        }
    }


    @Override
    public void init() {
        super.init();
        long uid = getOwnerUid();

        for (Scene scene : sceneMap.values()) {
            scene.init();
        }

        // 如果从未创建过大世界，在初始化的时候创建
        int mainSceneId = getDefaultWorldMainSceneId();
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

        // 初始化大世界上的场景队伍
        sceneTeam.init(ownPlayer, uid, mainSceneId);
    }

    @Override
    public void start() {
        super.start();
        for (Scene scene : sceneMap.values()) {
            scene.start();
        }
    }

    @Override
    public Scene createScene(int sceneId) {
        return createPersonalScene(sceneId);
    }

    // 创建玩家的个人场景(支持PlayerWorldScene/PlayerRoomScene)
    private Scene createPersonalScene(int sceneId)
    {
        Player player = getOwnPlayer();
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
        return getPlayerCount() >= MAX_PLAYER_COUNT;
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

    @Override
    public void playerPreEnter(Player player) {
        Player owner_player = getOwnPlayer();
        if (owner_player == null)
        {
            throw new UnknownLogicException("getOwnPlayer fails");
        }
        long uid = player.getUid();
        if (isWorldFull())
        {
            throw new UnknownLogicException("uid: " + uid + " playerPreEnter fails, world full");
        }
        if (slotInfoMap.containsKey(uid)) {
            log.warn("uid: " + uid + " already preEnter world, owner: " + owner_player.getUid());
        }

        WorldPlayerSlotInfo slotInfo = new WorldPlayerSlotInfo();
        slotInfo.uid = uid;
        slotInfo.preEnterTime = TimeUtils.currentTimeMillis();
        slotInfo.nickname = player.getBasicModule().getNickName();
        slotInfoMap.put(uid, slotInfo);
//        notifyPreEnterMpPlayer(uid, slot_info.nickname, proto::PlayerPreEnterMpNotify::START);

        log.debug("[WORLD] uid: " + uid + " pre-Enter world, owner: " + owner_player.getUid());
    }

    @Override
    public void playerEnter(Player player) {
        if (ownPlayer == null)
        {
            throw new UnknownLogicException("getOwnPlayer fails");
        }

        long uid = player.getUid();
        if (isWorldFull())
        {
            throw new UnknownLogicException("uid: " + uid + " playerEnter fails, world full, owner: " + ownPlayer.getUid());
        }
        WorldPlayerSlotInfo slotInfo = slotInfoMap.get(uid);
        if (slotInfo == null) {
            throw new UnknownLogicException("world owner: " + ownPlayer.getUid() + " cannot found slot for uid: " + uid);
        }

//        if (timer_ == null)
//        {
//            LOG_WARNING + "timer_ is null";
//            return -1;
//        }
//        if (!timer_.isActive() && 0 != timer_.startS(1, true, CODE_LOCATION))
//        {
//            LOG_WARNING + "timer_.startS fails";
//        }

        log.debug("[WORLD] uid: " + uid + " enter world, owner: " + ownPlayer.getUid());

        // 删除占位信息
        slotInfoMap.remove(uid);

//        // 释放主机发放给player的allow令牌
//        owner_player.getMpModule().removeEnterAllowPlayerUid(player.getUid());

        // 切换大世界
        player.getSceneModule().setCurWorld(this);
        player.getSceneModule().setCurWorldOwnerUid(ownPlayer.getUid());

//        boolean enterSelfWorld = owner_player.getUid() == uid;
//        // 更新匹配单元
//        if (!enterSelfWorld)
//        {
//            owner_player.getMatchModule().onGuestPlayerJoinMp(player);
//        }

        int oldCount = getPlayerCount();
        WorldPlayerInfo playerInfo = new WorldPlayerInfo();
        playerInfo.player = player;
        playerInfo.curSceneId = 0;
        playerInfo.enterTime = TimeUtils.currentTimeMillis();
        playerInfoMap.put(uid, playerInfo);

        sceneTeam.onPlayerEnter(player);

        //        // 更新联机状态
//        foreachPlayer([](GamePlayer & player)
//                {
//                        player.getMpModule().updateMpPlayerInfo();
//        return FOREACH_CONTINUE;
//    });

        // 同步客户端联机玩家数据
        notifyAllPlayerInfo();

        // 把当前大世界以及Owner相关数据同步给guest
        notifyWorldAndOwnerData(ownPlayer, player);

//        if (getPlayerCount() > 1)
//        {
//            PlayerWorldScene world_scene = getMainWorldScene();
//            if (world_scene != null)
//            {
//                // 更新怪物联机属性值
//                world_scene.refreshMonsterMpProp(getPlayerCount());
//
//                world_scene.getEncounterModule().onWorldPlayerChange(oldCount, getPlayerCount());
//            }
//
//            if (!isInMpMode())
//            {
//                // 设置world为联机模式
//                LOG_WARNING + "[MATCH] set world in mp mode not in join, uid: " + owner_player.getUid();
//                setIsInMpMode(true);
//                // 原地跳场景
//                owner_player.getSceneModule().jumpInplace();
//            }
//        }

//        // 给其他玩家发送世界人数变化消息:进入
//        BaseEvent event = MAKE_SHARED<WorldPlayerChangeEvent>(oldCount, getPlayerCount(), owner_player.getUid(), uid, true);
//        vector<uint32_t> enterer_uid_vec{uid};
//        BaseEvent mp_event = MAKE_SHARED<EnterMpEvent>(enterer_uid_vec);
//        vector<uint32_t> uid_vec;
//        // 通知其他人
//        foreachPlayer([uid, event, mp_event, &uid_vec](GamePlayer & player)
//                {
//            if (player.getUid() != uid)
//            {
//                player.getEventModule().notifyEvent(event);
//                player.getEventModule().notifyEvent(mp_event);
//                uid_vec.push_back(player.getUid());
//            }
//            return FOREACH_CONTINUE;
//        });
//        // 通知自己
//        if (!enterSelfWorld)
//        {
//            BaseEvent enterer_mp_event = MAKE_SHARED<EnterMpEvent>(uid_vec);
//            player.getEventModule().notifyEvent(enterer_mp_event);
//        }

        log.info( "uid: " + uid + " enter owner: " + ownPlayer.getUid() + " world");
    }

    @Override
    public void playerLeave(Player player, int leaveSceneId) {
        Player owner_player = getOwnPlayer();
        if (owner_player == null)
        {
            throw new UnknownLogicException("getOwnPlayer fails");
        }

        long uid = player.getUid();
        long owner_uid = owner_player.getUid();

        int old_count = getPlayerCount();
        WorldPlayerInfo leaveWorldPlayerInfo = playerInfoMap.remove(uid);

        if (leaveWorldPlayerInfo == null)
        {
            log.warn("Not found uid: " + uid + " in player_info_map");
        }
        player.getSceneModule().setCurWorld(null);
        player.getSceneModule().setCurWorldOwnerUid(0);

        // 更新匹配单元
//        if (owner_player.getUid() != uid)
//        {
//            owner_player.getMatchModule().onGuestPlayerLeaveMp(uid);
//        }

        // 通知各个Scene做进一步的清理工作
        for (Scene scene : sceneMap.values())
        {
            scene.onPlayerLeaveWorld(player);
        }

//        // 更新联机状态
//        foreachPlayer([](GamePlayer & player)
//                {
//                        player.getMpModule().updateMpPlayerInfo();
//        return FOREACH_CONTINUE;
//    });

        // 同步客户端联机玩家数据
        notifyAllPlayerInfo();

        // 清理SceneTeam数据结构, 清理玩家角色
        sceneTeam.onPlayerLeave(uid);

//        // 更新怪物联机属性值
//        PlayerWorldScene world_scene = getMainWorldScene();
//        if (isPlayerIn(owner_uid) && world_scene != null)
//        {
//            world_scene.refreshMonsterMpProp(getPlayerCount());
//            world_scene.getEncounterModule().onWorldPlayerChange(old_count, getPlayerCount());
//        }
//        // 给其他玩家发送大世界人数变化：退出
//        BaseEvent event = MAKE_SHARED<WorldPlayerChangeEvent>(old_count, getPlayerCount(), owner_player.getUid(), uid, false);
//        foreachPlayer([uid, event](GamePlayer & player)
//                {
//        if (player.getUid() != uid)
//            player.getEventModule().notifyEvent(event);
//        return FOREACH_CONTINUE;
//    });
//
//        player.getHuntingModule().onPlayerLeaveWorld(owner_player.getUid());

        log.info("[WORLD] uid: " + uid + " leave world, owner uid: " + owner_uid);
    }

    @Override
    public void onPlayerEnterScene(Player player, Scene scene) {
        long uid = player.getUid();
        WorldPlayerInfo playerInfo = playerInfoMap.get(uid);
        if (playerInfo == null)
        {
            log.error("player_info_map_ not found uid: " + uid);
            return;
        }

        log.debug("[WORLD] uid: " + uid + " enterScene: " + scene.getSceneId() + " scene owner: " + scene.getOwnerUid());

        playerInfo.curSceneId = scene.getSceneId();

        if (getOwnerUid() == uid && scene.getSceneType() == ProtoCommon.SceneType.SCENE_WORLD)
        {
            inSelfMainScene = true;
        }
    }

    @Override
    public void onPlayerLeaveScene(Player player, Scene scene) {
        long uid = player.getUid();
        WorldPlayerInfo playerInfo = playerInfoMap.get(uid);
        if (playerInfo == null)
        {
            log.error("player_info_map_ not found uid: " + uid);
            return;
        }

        log.debug("[WORLD] uid: " + uid + " leaveScene: " + scene.getSceneId() + " scene owner: " + scene.getOwnerUid());

        if (scene.getSceneType() == ProtoCommon.SceneType.SCENE_WORLD)
        {
//            Avatar avatar = player.getCurAvatar();
//            if (avatar != null)
//            {
//                player_info.last_main_pos = avatar.getPosition();
//                player_info.last_main_rot = avatar.getRotation();
//                player_info.is_pos_valid = true;
//            }
//            else
//            {
//                LOG_WARNING + "uid: " + uid + " getCurAvatar fails";
//            }
            if (getOwnerUid() == uid)
            {
                inSelfMainScene = false;
            }
        }
    }

    private void notifyWorldAndOwnerData(Player ownPlayer, Player player) {

    }

    @Override
    public void notifyWorldData(Player player) {
        if (!isPlayerIn(player.getUid()))
        {
            throw new UnknownLogicException("not find uid: " + player.getUid() + " in world");
        }

        // 同步客户端联机玩家数据
        notifyAllPlayerInfo();

        if (ownPlayer != null)
        {
            notifyWorldAndOwnerData(ownPlayer, player);
        }
    }
}
