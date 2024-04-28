package com.ares.game.scene.world;

import com.ares.common.math.Vector3;
import com.ares.common.util.ForeachPolicy;
import com.ares.core.utils.TimeUtils;
import com.ares.game.player.GamePlayer;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneUtil;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public abstract class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);

    public static class WorldPlayerSlotInfo
    {
        public long uid = 0;
        public long preEnterTime = 0;// 占位开始时间
        public String nickname;       // 昵称
    };

    public static class WorldPlayerInfo
    {
        public GamePlayer player;
        public int curSceneId = 0;
        public long enterTime = 0;    // 开始进入时间
        public Vector3 lastMainPos;      // 离开mainScene时的pos和rot
        public Vector3 lastMainRot;
        public boolean isPosValid = false;
    };

    protected int worldId = 0;
    protected long ownerUid = 0;
    protected Map<Integer, Scene> sceneMap; // 该世界包含的scene
    protected Map<Long, WorldPlayerSlotInfo> slotInfoMap;     // 准备进入的玩家
    protected Map<Long, WorldPlayerInfo> playerInfoMap;       // 当前在大世界中的玩家
    protected boolean inSelfMainScene = false;                // 主机是否在自己的main场景

    public World(int worldId) {
        this.worldId = worldId;
    }

    public int getWorldId() {
        return worldId;
    }

    public abstract ProtoCommon.WorldType getWorldType() ;

    public void init() {}
    public void start() {}

    public int getMainWorldSceneId() {
        return 1;
    }

    public Scene findScene(int sceneId) {
        return sceneMap.get(sceneId);
    }

    public Scene getScene(int sceneId) {
        Scene scene = findScene(sceneId);
        if (scene != null) {
            return scene;
        }
        return createScene(sceneId);
    }

    public abstract Scene createScene(int sceneId);

    public Scene getMainWorldScene() {
        return getScene(getMainWorldSceneId());
    }

    public long getOwnerUid() {
        return ownerUid;
    };

    public void setOwnerUid(long ownerUid) {
        this.ownerUid = ownerUid;
    }

    public int getPlayerCount() {
        return playerInfoMap.size();
    }

    public abstract boolean isWorldFull();

    public abstract int checkKickPlayer(long targetUid);
    public abstract void kickPlayer(long uid);

    public abstract void kickAllPlayer(int reason, boolean canKickOwner);

    // uid玩家是否在该大世界中
    public boolean isPlayerIn(int uid) {
        return playerInfoMap.containsKey(uid);
    }

    // 查找本世界的玩家
    GamePlayer findPlayer(long uid) {
        WorldPlayerInfo playerInfo = playerInfoMap.get(uid);
        if (playerInfo != null) {
            return playerInfo.player;
        }
        return null;
    }

    // 玩家准备进入大世界，占位
    public int playerPreEnter(GamePlayer player) {
        long uid = player.getUid();
        if (isWorldFull())
        {
            log.debug("uid: " + uid + " playerPreEnter fails, world full");
            return -1;
        }
        if (slotInfoMap.containsKey(uid)) {
            log.warn("uid: " + uid + " already preEnter world, owner: " + ownerUid);
        }
        WorldPlayerSlotInfo slotInfo = new WorldPlayerSlotInfo();
        slotInfo.uid = uid;
        slotInfo.preEnterTime = TimeUtils.currentTimeMillis();
        slotInfo.nickname = player.getBasicModule().getNickName();
        slotInfoMap.put(uid, slotInfo);

        log.debug("[WORLD] uid: " + uid + " pre-Enter world, owner: " + ownerUid);
        return 0;
    }

    // 玩家进入大世界
    public int playerEnter(GamePlayer player) {
        return 0;
    }

    // 玩家离开大世界，已经完成退场流程
    public int playerLeave(GamePlayer player, int leaveSceneId) {
        return 0;
    }

    // 玩家进入某个scene
    public void onPlayerEnterScene(GamePlayer player, Scene scene) {
        long uid = player.getUid();
        WorldPlayerInfo playerInfo = playerInfoMap.get(uid);
        if (playerInfo == null)
        {
            log.debug("player_info_map_ not found uid: " + uid + " owner:" + getOwnerUid());
            return;
        }

        log.debug("[WORLD] uid: " + uid + " enterScene: " + scene.getSceneId() + " scene owner: " + scene.getOwnerUid());

        playerInfo.curSceneId = scene.getSceneId();
    }

    // 玩家退出某个scene
    public void onPlayerLeaveScene(GamePlayer player, Scene scene) {
        long uid = player.getUid();
        WorldPlayerInfo playerInfo = playerInfoMap.get(uid);
        if (playerInfo == null)
        {
            log.debug("player_info_map_ not found uid: " + uid + " owner:" + getOwnerUid());
            return;
        }

        log.debug("[WORLD] uid: " + uid + " leaveScene: " + scene.getSceneId() + " scene owner: " + scene.getOwnerUid());
        if (SceneUtil.isWorldScene(scene.getSceneType()))
        {
//            AvatarPtr avatar_ptr = player.getCurAvatar();
//            if (avatar_ptr != nullptr)
//            {
//                player_info.last_main_pos = avatar_ptr->getPosition();
//                player_info.last_main_rot = avatar_ptr->getRotation();
//                player_info.is_pos_valid = true;
//            }
//            else
//            {
//                LOG_WARNING << "uid: " << uid << " getCurAvatar fails";
//            }
        }
    }

    public int foreachPlayer(Function<GamePlayer, ForeachPolicy> func) {
        List<GamePlayer> players = new ArrayList<GamePlayer>();
        for (WorldPlayerInfo playerInfo : playerInfoMap.values()) {
            players.add(playerInfo.player);
        }
        for (GamePlayer player : players)
        {
            if (func.apply(player) != ForeachPolicy.CONTINUE)
            {
                return 1;
            }
        }
        return 0;
    }

    Set<Long> getWorldPlayerUidSet() {
        return playerInfoMap.keySet();
    }

    // 同步大世界中玩家的基本数据
    protected void notifyAllPlayerInfo() {
    }
}
