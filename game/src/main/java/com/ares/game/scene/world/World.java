package com.ares.game.scene.world;

import com.ares.common.math.Vector3;
import com.ares.common.util.ForeachPolicy;
import com.ares.game.player.Player;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneTeam;
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
        public Player player;
        public int curSceneId = 0;
        public long enterTime = 0;    // 开始进入时间
        public Vector3 lastMainPos;      // 离开mainScene时的pos和rot
        public Vector3 lastMainRot;
        public boolean isPosValid = false;
    };

    protected long ownerUid = 0;
    protected final Map<Integer, Scene> sceneMap = new HashMap<>(); // 该世界包含的scene
    protected final Map<Long, WorldPlayerSlotInfo> slotInfoMap = new HashMap<>();     // 准备进入的玩家
    protected final Map<Long, WorldPlayerInfo> playerInfoMap = new HashMap<>();       // 当前在大世界中的玩家
    protected boolean inSelfMainScene = false;                // 主机是否在自己的main场景
    protected final SceneTeam sceneTeam = new SceneTeam();      // 场景队伍

    public World() {
    }

    public abstract ProtoCommon.PbWorldType getWorldType() ;

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

    public SceneTeam getSceneTeam() {
        return sceneTeam;
    }

    public abstract boolean isWorldFull();

    public abstract int checkKickPlayer(long targetUid);
    public abstract void kickPlayer(long uid);

    public abstract void kickAllPlayer(int reason, boolean canKickOwner);

    // uid玩家是否在该大世界中
    public boolean isPlayerIn(long uid) {
        return playerInfoMap.containsKey(uid);
    }

    // 查找本世界的玩家
    Player findPlayer(long uid) {
        WorldPlayerInfo playerInfo = playerInfoMap.get(uid);
        if (playerInfo != null) {
            return playerInfo.player;
        }
        return null;
    }

    // 玩家准备进入大世界，占位
    public void playerPreEnter(Player player) {
    }

    // 玩家进入大世界
    public void playerEnter(Player player) {
    }

    // 玩家离开大世界，已经完成退场流程
    public void playerLeave(Player player, int leaveSceneId) {
    }

    // 玩家进入某个scene
    public void onPlayerEnterScene(Player player, Scene scene) {
    }

    // 玩家退出某个scene
    public void onPlayerLeaveScene(Player player, Scene scene) {
    }

    public int foreachPlayer(Function<Player, ForeachPolicy> func) {
        List<Player> players = new ArrayList<Player>();
        for (WorldPlayerInfo playerInfo : playerInfoMap.values()) {
            players.add(playerInfo.player);
        }
        for (Player player : players)
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

    // 断线重连，重新同步world的数据
    public void notifyWorldData(Player player) {
    }
}
