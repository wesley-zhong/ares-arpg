package com.ares.game.scene;

import com.ares.common.math.Transform;
import com.ares.common.math.Vector3;
import com.ares.common.util.ForeachPolicy;
import com.ares.core.exception.FyLogicException;
import com.ares.core.exception.UnknownLogicException;
import com.ares.core.utils.TimeUtils;
import com.ares.game.player.Player;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.entity.EntityMgr;
import com.ares.game.scene.entity.EntitySightGroup;
import com.ares.game.scene.entity.EntityUtils;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.entity.avatarteam.AvatarTeamEntity;
import com.ares.game.scene.entity.eyepoint.PlayerEyePoint;
import com.ares.game.scene.entity.scene.SceneEntity;
import com.ares.game.scene.modules.sight.SceneSightModule;
import com.ares.game.scene.visitor.VisitEntityVisitor;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.*;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

@Getter
@Setter
@Slf4j
public abstract class Scene extends SceneModuleContainer {
    public static int MAX_PLAYER_COUNT = 4;
    public static int MAX_NEXT_ENTITY_INDEX = (1 << 24) - 1;

    public static class SceneInitParam {
        public int dungeonId;
    }

    static class ScenePlayerPeerInfo {
        long uid = 0;
        long enterTime = 0;   // Peer的分配时间
    }

    ;

    public static class ScenePlayerLocation {
        public Vector3 curPos;           // 当前位置
        public Vector3 curRot;           // 当前朝向
        public Vector3 lastValidPos;    // 最后一次处于正常状态(站、走、跑)的坐标
        public Vector3 lastValidRot;    // 最后一次处于正常状态(站、走、跑)的朝向
    }

    ;

    // 检查区域的类型
    enum CheckRegionType {
        CHECK_REGION_NONE,
        CHECK_REGION_MOVE,
        CHECK_REGION_BORN,
        CHECK_REGION_LEAVE,
    }

    ;

    // 检查区域的参数
    static class CheckRegionParam {
        CheckRegionType type;
        Vector3 prevPos;
        int prevRoom = 0;

        public CheckRegionParam(CheckRegionType type) {
            this.type = type;
        }
    }

    ;

    private final int sceneId;
    private Player ownPlayer;
    private long ownerUid;
    private long beginTimeMs;
    private Player hostPlayer;      // 主机玩家
    private final Map<Integer, Entity> entityMap = new HashMap<>();      // 所有的实体
    private final Map<Integer, Entity> gridlessEntityMap = new HashMap<>();      // 不在格子上的实体
    private final Map<Integer, Entity> deadEntityMap = new HashMap<>();      // 不在格子上的实体
    private final Map<Long, Player> playerMap = new HashMap<>();
    private final Map<Long, PlayerViewMgr> playerViewMgrMap = new HashMap<>();      // 所有的玩家的视野管理器
    private final Map<Long, ScenePlayerLocation> playerLocationMap = new HashMap<>();
    private ScenePlayerLocation ownerLocation;
    private final Map<Integer, ScenePlayerPeerInfo> peerMap = new HashMap<>();
    private SceneEntity sceneEntity;
    private final Object entityIdLock = new Object();
    private int nextEntityIndex = 0;

    public Scene(final int sceneId) {
        this.sceneId = sceneId;
    }

    public abstract ProtoCommon.SceneType getSceneType();

    private SceneSightModule sightModule;

    public SceneSightModule getSightModule() {
        if (sightModule == null) {
            sightModule = (SceneSightModule) getModule(ProtoInner.GameModuleId.GMI_SceneSight);
        }
        return sightModule;
    }

    public void setOwnPlayer(Player player) {
        this.ownPlayer = player;
        if (player != null) {
            ownerUid = player.getUid();
        }
    }

    public void fromBin(BinServer.SceneBin bin) {

    }

    public void toBin(BinServer.SceneBin.Builder bin) {
        bin.setSceneId(sceneId);
    }

    public void init() {
        init(new SceneInitParam());
    }

    public void init(SceneInitParam initParam) {
    }

    public void start() {

    }


    public void setPlayerLocation(long uid, ScenePlayerLocation location) {
        log.debug("[ENTER_SCENE] setPlayerLocation, last_valid_pos:" + location.lastValidPos + " cur_pos:" + location.curPos);
        playerLocationMap.put(uid, location);
        if (uid == getOwnerUid()) {
            ownerLocation = location;
        }

//        PlayerViewMgr player_view_mgr = findPlayerViewMgr(uid);
//        if (player_view_mgr == null || !player_view_mgr.isHasEyePointAsEye())
//        {
//            getBlockGroupComp().onPlayerEyePosChange(uid, location.cur_pos);
//        }
    }

    public void erasePlayerLocation(long uid) {
        playerLocationMap.remove(uid);
//        getBlockGroupComp().erasePlayerReloadPos(uid);
    }

    public ScenePlayerLocation getPlayerLocation(long uid) {
        return playerLocationMap.get(uid);
    }

    public Transform getPlayerValidLocation(long uid) {
        ScenePlayerLocation location = playerLocationMap.get(uid);
        if (location == null) {
            return new Transform(Vector3.ZERO, Vector3.ZERO);
        }
        return new Transform(location.lastValidPos, location.lastValidRot);
    }

    public World getOwnWorld() {
        return null;
    }

    public PlayerWorld getOwnPlayerWorld() {
        return null;
    }

    // 获取玩家的数量
    public int getPlayerCount() {
        return playerMap.size();
    }

    public Set<Long> getPlayerUidSet() {
        return new HashSet<>(playerMap.keySet());
    }

    public Player findPlayer(long uid) {
        return playerMap.get(uid);
    }

    // 设置主机玩家
    public void setHostPlayer(Player player) {
        if (hostPlayer == player) {
            log.debug("host player is the same");
            return;
        }
        hostPlayer = player;
        if (player == null) {
            return;
        }
//        proto::HostPlayerNotify notify;
//        notify.set_host_uid(player.getUid());
//        notify.set_host_peer_id(getPeerId(player.getUid()));
//        notifyAllPlayer(notify);
//
//        // 切MPLevelEntity的authority
//        if (mp_level_entity_ != null)
//        {
//            std::vector<GamePlayer> player_vec;
//            foreachPlayer([player_vec](GamePlayer  player)
//                {
//                        player_vec.emplace_back(toThis(player));
//            return FOREACH_CONTINUE;
//        });
//            mp_level_entity_.onAuthorityChangedToPlayer(player, player_vec);
//        }
    }

    // 获取peer_id对应的玩家
    public Player findPeerPlayer(int peerId) {
        ScenePlayerPeerInfo peerInfo = peerMap.get(peerId);
        if (peerInfo == null) {
            log.debug("can't find peer_id:" + peerId);
            return null;
        }
        return findPlayer(peerInfo.uid);
    }

    // 获得uid对应的peer_id
    public int getPeerId(long targetUid) {
        for (Map.Entry<Integer, ScenePlayerPeerInfo> entry : peerMap.entrySet()) {
            if (entry.getValue().uid == targetUid) {
                return entry.getKey();
            }
        }

        return 0;
    }

    // 获取主机PeerId
    public int getHostPeerId() {
        Player hostPlayer1 = getHostPlayer();
        if (hostPlayer1 == null) {
            return 0;
        }

        return getPeerId(hostPlayer1.getUid());
    }


    // 玩家预进入场景
    public void playerPreEnter(Player player) {
        long uid = player.getUid();
        if (0 != getPeerId(uid)) {
            // 更新host_player
            if (hostPlayer == null) {
                setHostPlayer(player);
            }
            log.debug("peerId exist in scene, scene_id:" + sceneId + ", uid:" + uid);
            return;
        }

        if (peerMap.size() >= MAX_PLAYER_COUNT) {
            log.debug("scene is full, scene_id:" + sceneId + ", uid:" + uid);
            throw new FyLogicException(ProtoErrorCode.ErrCode.SCENE_IS_FULL_VALUE);
        }

        int peerId = 1;
        for (Integer key : peerMap.keySet()) {
            // 找到第一个未被占用的peer_id
            if (!key.equals(peerId)) {
                break;
            }
            ++peerId;
        }
        if (peerId > 7) {
            throw new UnknownLogicException("invalid peerId:" + peerId);
        }

        ScenePlayerPeerInfo peerInfo = new ScenePlayerPeerInfo();
        peerInfo.uid = uid;
        peerInfo.enterTime = TimeUtils.currentTimeMillis();
        peerMap.put(peerId, peerInfo);

        // 更新host_player
        if (hostPlayer == null) {
            setHostPlayer(player);
        }

        log.info("[EnterScene] player preEnter scene, scene_id:" + sceneId + ", peerId:" + peerId + ", uid:" + uid);

        // 更新场景主人的联机状态
//        GamePlayer owner_player = getOwnPlayer();
//        if (null != owner_player)
//        {
//            owner_player.getMpComp().updateMpPlayerInfo();
//        }
    }

    // 玩家进入场景
    public void playerEnter(Player player, Vector3 pos, Vector3 rot, boolean reLogin, List<Avatar> enterSceneAvatarList, Avatar appearAvatar) {
        World world = player.getSceneModule().getCurWorld();
        if (world == null) {
            if (getSceneType() != ProtoCommon.SceneType.SCENE_DUNGEON) {
                throw new UnknownLogicException("uid: " + player.getUid() + " getCurWorld is null");
            } else {
                // 进入地城场景时，当前world可能为null
                // 发生场景：联机地城主机掉线后，客机重连或者坠落后
                log.debug("uid: " + player.getUid() + " enter dungeon, cur world is null");
            }
        }
        // 目标是地城时，world 和 scene 的 owner 可能不一样
        // 发生场景：联机地城中，主机退出地城，地城的owner改变，主机未下线，world的owner依然为主机
        if (world != null && getSceneType() != ProtoCommon.SceneType.SCENE_DUNGEON && world.getOwnerUid() != getOwnerUid()) {
            throw new UnknownLogicException("uid: " + player.getUid() + " world owner: " + world.getOwnerUid() + " scene owner: " + getOwnerUid());
        }
        if (appearAvatar == null) {
            throw new UnknownLogicException("uid: " + player.getUid() + " appearAvatar is null");
        }

        long uid = player.getUid();
        int peerId = getPeerId(uid);
        if (0 == peerId) {
            throw new UnknownLogicException("peerId not exist in scene, scene_id:" + sceneId + ", uid:" + uid);
        }

        SceneTeam sceneTeam = findSceneTeam();
        if (sceneTeam == null) {
            throw new UnknownLogicException("scene team  is null scene_id:" + getSceneId());
        }

        if (null == findPlayer(uid)) {
            playerMap.put(uid, player);
        }
        // 当当前无TeamEntity或指针不相等时添加
        AvatarTeamEntity curTeamEntity = player.getAvatarModule().getTeamEntity();
        if (curTeamEntity == null || curTeamEntity.getScene() != this) {
            addAvatarTeamEntity(player);
        }

        List<Long> teamAvatarGuidList = new ArrayList<>();
        for (Avatar avatar : enterSceneAvatarList) {
            teamAvatarGuidList.add(avatar.getGuid());
        }

        // 警告代码, 上一个角色未被回收！
        Avatar curAvatar = player.getCurAvatar();
        if (curAvatar != null && curAvatar.isOnScene()) {
            log.error("[ENTER]last cur avatar is on scene!, uid:" + uid);
        }
        // 设置cur_scene
        Scene lastCurScene = player.getSceneModule().getCurScene();
        player.getSceneModule().setCurScene(this);
        // 设置进场角色为当前角色
        player.getAvatarModule().setCurAvatar(appearAvatar);
        // 这里进入的玩家不用通知，其他的玩家需要通知，通知要在WorldPlayerInfoNotify之后
        List<Long> oldGuidList = sceneTeam.getAvatarGuidList(uid);
//        RefreshAbilityGuard guard;
//        for (auto guid : teamAvatarGuidList)
//        {
//            guard.addCreature(player.getAvatarComp().findAvatar(guid));
//        }
        sceneTeam.setPlayerAvatarTeamAndAddToScene(uid, teamAvatarGuidList, appearAvatar.getGuid(), this, getChangeSceneTeamReason(), false);
        List<Long> nowGuidList = sceneTeam.getAvatarGuidList(uid);
        for (long guid : oldGuidList) {
            // 在队伍中, 跳过
            if (nowGuidList.contains(guid)) {
                continue;
            }
            Avatar avatar = player.getAvatarModule().findAvatar(guid);
            // 不在队伍中
            // 尝试从当前场景踢走
            if (avatar != null && avatar.getScene() == this) {
                if (avatar == player.getCurAvatar()) {
                    log.error("try delete avatar is cur avatar, this should not happen, uid:" + player.getUid() + " avatar:" + avatar);
                    continue;
                }
                delAvatarAndWeaponEntity(avatar);
            }
        }

        if (getOwnerUid() == player.getUid()) {
            // 在自己的世界里面记录scene_id
            ProtoCommon.SceneType scene_type = getSceneType();
            if (SceneUtil.isPlayerScene(scene_type)) {
                player.getSceneModule().setMyCurSceneId(getSceneId());
            }
        }
        if (this != lastCurScene || reLogin) {
            notifySceneAndHostData(uid);
            player.notifyPlayerEnterSceneInfo();
        }

        // 默认进入是valid_position
        ScenePlayerLocation playerLocation = new ScenePlayerLocation();
        playerLocation.curPos = pos;
        playerLocation.curRot = rot;
        playerLocation.lastValidPos = pos;
        playerLocation.lastValidRot = rot;
        setPlayerLocation(uid, playerLocation);

        // 广播当前场景的玩家信息
        notifyAllPlayerInfo();
        if (world != null) {
            world.onPlayerEnterScene(player, this);
        }
//
//        if (timer_ != null)
//        {
//            if (!timer_.isActive()  0 != timer_.startS(1, true, CODE_LOCATION))
//            {
//                log._ERROR + "addTimer fails";
//                return -1;
//            }
//        }

        log.info("[EnterScene] player Enter scene, scene_id:" + sceneId + ", peerId:" + peerId + ", uid:" + uid);
    }

    private void notifySceneAndHostData(long uid) {

    }

    // 玩家离开场景(可能去了大世界其他场景)
    public void playerLeave(long uid) {
        Player player = findPlayer(uid);
        if (player == null) {
            log.warn("findPlayer failed, uid:" + uid + " scene_id:" + getSceneId());
            return;
        }

        World world = player.getSceneModule().getCurWorld();
        if (getSceneType() != ProtoCommon.SceneType.SCENE_DUNGEON) {
            // 地城场景中world可能为空，发生在主机下线时
            if (world == null) {
                log.warn("getCurWorld fails, uid: " + uid);
                return;
            }
            // 地城场景中owner可能和world owner不一致，发生在主机退出地城时
            else if (getOwnerUid() != world.getOwnerUid()) {
                log.error("uid: " + player.getUid() + " world owner: " + world.getOwnerUid() + " scene owner: " + getOwnerUid());
                return;
            }
        }

        // 清理TeamEntity
        delAvatarTeamEntity(player);
//        // 清理生成器
//        auto spawner_iter = wind_seed_spawner_map_.find(player.getUid());
//        if (spawner_iter != wind_seed_spawner_map_.end()  spawner_iter.second != null)
//        {
//            spawner_iter.second.delPlayer(player.getUid());
//            wind_seed_spawner_map_.erase(player.getUid());
//        }
//        std::vector<AvatarWtr> avatar_vec = player.getAvatarComp().getAllAvatarVec();
//        for (AvatarWtr avatar_wtr : avatar_vec)
//        {
//            Avatar avatar = avatar_wtr.lock();
//            if (null == avatar)
//            {
//                continue;
//            }
//            if (0 != delAvatarAndWeaponEntity(*avatar))
//            {
//                log._WARNING + "delAvatarAndWeaponEntity fails" + *avatar;
//            }
//        }

        int peerId = getPeerId(uid);
        if (peerMap.remove(peerId) == null) {
            log.warn("can't find peerId:" + peerId);
        }
//        peer_authority_entity_map_.erase(peerId);
        if (playerMap.remove(uid) == null) {
            log.warn("can't find playerId:" + uid);
        }

        Player hostPlayer1 = getHostPlayer();
        if (hostPlayer1 == player) {
            Player newHost = null;
            for (Player p : playerMap.values()) {
                if (p != null && p.isConnected()) {
                    newHost = p;
                    break;
                }
            }
            setHostPlayer(newHost);
        }

//        BaseEvent event = MAKE_SHARED<LeaveSceneEvent>(scene_id_);
//        if (event != null)
//        {
//            player.getEventComp().notifyEvent(event);
//        }

//        // 清理相关数据
//        getMiscComp().onPlayerLeave(uid);
//        getMultistagePlayComp().onLeaveScene(player);
//        getGalleryComp().onLeaveScene(player);
//        player.getSceneModule().playerLeaveScene();

        // 广播当前场景的玩家信息
        notifyAllPlayerInfo();

        log.debug("[PEER] del peerId:" + peerId + ", scene_id:" + sceneId + ", uid:" + uid);

        if (world != null) {
            world.onPlayerLeaveScene(player, this);
            // 判断是否要离开当前大世界 （destScene == null 表示登出）
            Scene destScene = player.getSceneModule().getDestScene();
            // 登出 || 目的场景与当前world不属于同一owner（目的场景不是地城）
            // 目的场景为地城时目前好像也不会出现owner不一样？
            if (destScene == null || (destScene.getSceneType() != ProtoCommon.SceneType.SCENE_DUNGEON && destScene.getOwnWorld() != world)) {
                world.playerLeave(player, sceneId);
                return;
            }
        }

//        // 手动刷新一下当前SceneTeam的队伍共鸣，当前不在场景里的玩家也要被通知到
//        SceneTeam scene_team = findSceneTeam();
//        if (scene_team == null)
//        {
//            log._WARNING + "scene team  is null scene_id:" + getSceneId();
//            return;
//        }
//        scene_team.refreshCurTeamResonances();
//        scene_team.notifySceneTeamUpdate();
    }

    // 对每一个玩家调用func
    public int foreachPlayer(Function<Player, ForeachPolicy> func) {
        Player[] players = playerMap.values().toArray(new Player[playerMap.size()]);

        for (Player player : players) {
            if (func.apply(player) != ForeachPolicy.CONTINUE) {
                return 1;
            }
        }
        return 0;
    }

    // 玩家离开大世界, 进一步的清理工作
    public void onPlayerLeaveWorld(Player player) {
        long uid = player.getUid();
        Player owner = getOwnPlayer();
        // 如果不是主人，就从当前场景删除位置
        if (owner == null || owner.getUid() != uid) {
            erasePlayerLocation(uid);
        }
//
//        // play_team_entity是跟随玩家创建的,兜底删除
//        delPlayTeamEntity(player);
    }

    // 同步本场景全部玩家信息
    void notifyAllPlayerInfo() {
        ProtoScene.ScenePlayerInfoNtf.Builder notify = ProtoScene.ScenePlayerInfoNtf.newBuilder();
        for (Player player : playerMap.values()) {
            notify.addPlayerInfoList(player.getScenePlayerInfo());
        }
        // 一人时也要发送
        notifyAllPlayer(ProtoMsgId.MsgId.SCENE_PLAYER_INFO_NTF, notify.build(), 0);
    }

    // 将协议发送给所有玩家
    void notifyAllPlayer(ProtoMsgId.MsgId msgId, Message message, long excludeUid) {
        for (Player player : playerMap.values()) {
            if (excludeUid != 0 && player.getUid() == excludeUid)
                continue;
            player.sendMessage(msgId, message);
        }
    }

    public PlayerViewMgr findPlayerViewMgr(long uid) {
        return playerViewMgrMap.get(uid);
    }

    public void insertPlayerViewMgr(Player player) {
        PlayerViewMgr viewMgr = new PlayerViewMgr(this, player);
        playerViewMgrMap.put(player.getUid(), viewMgr);
    }

    public void erasePlayerViewMgr(long uid) {
        playerViewMgrMap.remove(uid);
    }

    // 生成新的实体ID
    public int genNewEntityId(ProtoScene.ProtEntityType type) {
        boolean is_rewind = false;
        int nextEntityIndex = 0;
        {
            synchronized (entityIdLock) {
                if (this.nextEntityIndex >= MAX_NEXT_ENTITY_INDEX) {
                    nextEntityIndex = this.nextEntityIndex = 1;
                    is_rewind = true;
                } else {
                    nextEntityIndex = ++this.nextEntityIndex;
                }
            }
        }
        if (is_rewind) {
            log.error("next_entity_index_ rewind. scene_id:" + sceneId + " owner_uid:" + getOwnerUid() + " scene_time_ms:" + getSceneTimeMs());
        }
        return EntityUtils.getEntityId(type, nextEntityIndex);
    }

    public long getSceneTimeMs() {
        return 0;
    }

    public Entity getPlayerEyeEntity(long uid) {
        PlayerViewMgr viewMgr = findPlayerViewMgr(uid);
        if (viewMgr == null) {
            throw new UnknownLogicException("viewMgr not found. uid:" + uid + " scene_id:" + getSceneId());
        }

        PlayerEyePoint eye_point = viewMgr.getEyePointAsEye();
        if (eye_point != null && eye_point.isOnScene()) {
            return eye_point;
        }

        Player player = findPlayer(uid);
        if (player == null) {
            throw new UnknownLogicException("player not found. uid:" + uid + " scene_id:" + getSceneId());
        }

        Avatar avatar = player.getCurAvatar();
        if (avatar != null && avatar.isOnScene()) {
            return avatar;
        }

        throw new UnknownLogicException("player has no eye_entity. uid:" + uid + " scene_id:" + getSceneId());
    }

    // 通过判断view_entity获取结果集合中的所有玩家uid
    public Set<Long> getPlayerUidSetByViewEntity(Collection<Entity> entities, long excludeUid) {
        if (entities == null) {
            return Set.of();
        }

        Set<Long> result = new HashSet<>();
        for (Entity entity : entities) {
            Player player = entity.getPlayer();
            if (player != null) {
                long uid = player.getUid();
                if (excludeUid != 0 && excludeUid == uid) {
                    continue;
                }

                Entity player_eye_entity = getPlayerEyeEntity(uid);
                if (player_eye_entity == null) {
                    log.warn("uid:{} get eye_entity failed. entity:{}", player.getUid(), entity);
                    continue;
                }

                if (player_eye_entity == entity) {
                    result.add(uid);
                }
            }
        }
        return result;
    }

    // 获取在范围内的所有角色
    public Collection<Avatar> getAvatarsInRange(Vector3 pos, float visionRange) {
        List<Avatar> result = new ArrayList<>();
        foreachPlayer(player -> {
            Avatar avatar = player.getCurAvatar();
            if (avatar != null && Vector3.getDistance(avatar.getPosition(), pos) < visionRange) {
                result.add(avatar);
            }
            return ForeachPolicy.CONTINUE;
        });
        return result;
    }


    // 实体在场景上出现
    public void entityAppear(Entity entity, VisionContext context) {
//        Config config = GAME_SERVICE.getConfig();
//        if (config != null)
//        {
//            int size = entity_map_.size();
//            if (size >= config.warn_scene_entity_limit_num)
//            {
//                log._WARNING + "EntityMap size >= WarnLimitNum:" + config.warn_scene_entity_limit_num + *this;
//            }
//            if (size >= config.error_scene_entity_limit_num)
//            {
//                log._ERROR + "EntityMap size >= ErrorLimitNum:" + config.error_scene_entity_limit_num + *this;
//                return -1;
//            }
//        }

        Collection<Entity> meetEntities = getSightModule().placeEntity(entity);
//#ifdef HK4Edebug(
        StringBuilder sb = new StringBuilder();
        for (Entity entity1 : meetEntities) {
            sb.append(entity1.getEntityId()).append(',');
        }
        log.debug("[FY] meet entity vec: " + sb + " " + entity + " @ " + entity.getCoordinate().toString());
//#endif

        Set<Long> uidSet = getPlayerUidSetByViewEntity(meetEntities, 0);
        if (entityMap.put(entity.getEntityId(), entity) != null) {
            throw new UnknownLogicException("entity already exists:" + entity);
        }

        Player player = null;
        ProtoScene.ProtEntityType entity_type = entity.getEntityType();
        if (entity_type == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR) {
            Avatar avatar = (Avatar) entity;
            player = entity.getPlayer();
            if (player == null) {
                throw new UnknownLogicException("player not found. uid:" + player.getUid() + " scene_id:" + getSceneId());
            }

            long uid = player.getUid();
            uidSet.add(uid);

            ScenePlayerLocation playerLocation = new ScenePlayerLocation();
            playerLocation.curPos = avatar.getPosition();
            playerLocation.curRot = avatar.getRotation();
            playerLocation.lastValidPos = avatar.getLastValidPos();
            playerLocation.lastValidRot = avatar.getLastValidRot();
            setPlayerLocation(uid, playerLocation);
            avatar.setLastValidPos(playerLocation.lastValidPos);
            avatar.setLastValidRot(playerLocation.lastValidRot);
        } else if (entity_type == ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT) {
            player = entity.getPlayer();
            if (player == null) {
                throw new UnknownLogicException("player not found. uid:" + player.getUid() + " scene_id:" + getSceneId());
            }

            if (!SceneUtil.isEyePointAsEyeClosed()) {
                PlayerViewMgr player_view_mgr = findPlayerViewMgr(player.getUid());
                if (player_view_mgr != null) {
                    player_view_mgr.clearViewContainer();
                } else {
                    log.warn("player_view_mgr is null. uid:" + player.getUid());
                }
            }

            // 自身avatar不需要检查距离,一律能看见
            Avatar avatar = player.getCurAvatar();
            if (avatar != null && avatar.isOnScene()) {
                meetEntities.add(avatar);
            }
        }

        EntitySightGroup preInstallSightGroup = entity.getPreInstallSightGroup();
        if (preInstallSightGroup != null) {
            preInstallSightGroup.addEntityInSightGroup(entity);
            for (Player groupViewPlayer : preInstallSightGroup.getViewingPlayers()) {
                if (!uidSet.contains(groupViewPlayer.getUid())) {
                    notifyEntityAppear(groupViewPlayer, List.of(entity), context);
                }
            }
        }
        VisionContext appearContext = new VisionContext(context);
        if (appearContext.type == ProtoScene.VisionType.VISION_MEET) {
            appearContext.type = ProtoScene.VisionType.VISION_BORN;
        }
        entityEnterPlayersSightRange(uidSet, entity, appearContext);

        boolean isReplace = SceneUtil.isVisionTypeReplace(context.type);
        if (!isReplace && player != null) {
            entitiesEnterPlayerSightRange(player, meetEntities, context);
        }

        checkRegion(entity, context.type, new CheckRegionParam(CheckRegionType.CHECK_REGION_BORN));
    }

    // 实体在场景上消失
    public void entityDisappear(Entity entity, VisionContext context, long dead_clear_time) {
        if (!entity.isOnScene()) {
            log.debug("entity is not on scene" + entity);
            return;
        }

        Scene scene = entity.getScene();
        if (scene != this) {
            throw new UnknownLogicException("entity is not on this scene" + entity);
        }

        getSightModule().removeEntity(entity);

        Set<Long> uidSet = new HashSet<>();
        Collection<Player> viewingPlayers = entity.getViewMgr().getViewingPlayers();
        for (Player player : viewingPlayers) {
            uidSet.add(player.getUid());
        }

        // 角色为非角色时，场景设置为null，角色在playerLeave时，设置为空
        if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR) {
            entity.setScene(null);
        }
        if (entityMap.remove(entity.getEntityId()) == null) {
            throw new UnknownLogicException("entity not exists " + entity);
        }
        // 有亡语
        if (dead_clear_time > 0) {
            addDeadEntity(entity, dead_clear_time);
        }

        boolean isReplace = SceneUtil.isVisionTypeReplace(context.type);
        Player player = null;
        ProtoScene.ProtEntityType entityType = entity.getEntityType();
        if (entityType == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR) {
            Avatar avatar = (Avatar) entity;
//            avatar.stopTimer();
            avatar.clearMotionState();

            player = entity.getPlayer();
            if (player != null) {
                if (isReplace) {
                    // 正常情况下此时uid_set应该是包含了自身uid的，这里再做个保险
                    uidSet.add(player.getUid());
                } else {
                    uidSet.remove(player.getUid());
                }
            }
        }

        entityLeavePlayersSightRange(uidSet, entity, context);

        EntitySightGroup sight_group = entity.getSightGroup();
        if (sight_group != null) {
            sight_group.delEntityInSightGroup(entity);
            for (Player groupViewPlayer : sight_group.getViewingPlayers()) {
                notifyEntityDisappear(groupViewPlayer, List.of(entity), context);
            }
        }

        if (!isReplace && entityType == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR && player != null) {
            PlayerViewMgr player_view_mgr = findPlayerViewMgr(player.getUid());
            if (player_view_mgr != null) {
                entitiesLeavePlayerSightRange(player, player_view_mgr.getEntitiesInView(), VisionContext.MISS);
            } else {
                throw new UnknownLogicException("view_mgr is null . uid" + player.getUid());
            }
        }

        checkRegion(entity, context.type, new CheckRegionParam(CheckRegionType.CHECK_REGION_LEAVE));

//        checkEnterWeatherArea(entity, context.type, check_param);
//
//        Group group = entity.getGroup();
//        if (group != null)
//        {
//            group.delEntity(entity, context.type);
//        }
//        else if (entity.getConfigId() != 0)
//        {
//            log.debug( + "group is null, config_id: " + entity.getConfigId();
//        }
    }

    // 实体移动到目的位置
    public void entityMoveTo(Entity entity, Vector3 pos) {
        if (!entity.isOnScene()) {
            throw new UnknownLogicException("entity is not on scene" + entity);
        }
        Scene scene = entity.getScene();
        if (scene != this) {
            throw new UnknownLogicException("entity is not on this scene" + entity);
        }
        if (0 != entity.checkPosition(pos)) {
            throw new UnknownLogicException("scene_id: " + scene + "checkPosition fails pos:" + pos + " entity:" + entity);
        }

        SceneSightModule.EntityMoveToRet entityMoveToRet = getSightModule().entityMoveTo(entity, pos);

        Set<Long> missUidSet = getPlayerUidSetByViewEntity(entityMoveToRet.missEntities, 0);
        Set<Long> meetUidSet = getPlayerUidSetByViewEntity(entityMoveToRet.meetEntities, 0);

        Vector3 prev_pos = entity.getPosition();
        entity.setPosition(pos);

        entityLeavePlayersSightRange(missUidSet, entity, VisionContext.MISS);
        entityEnterPlayersSightRange(meetUidSet, entity, VisionContext.MEET);
        do {
            if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR) {
                break;
            }

            Player player = entity.getPlayer();
            if (player == null) {
                break;
            }

            PlayerViewMgr player_view_mgr = findPlayerViewMgr(player.getUid());
            if (player_view_mgr == null) {
                log.warn("uid:{} view_mgr is null", player.getUid());
                break;
            }

            if (player_view_mgr.isHasEyePointAsEye()) {
                break;
            }

            entitiesLeavePlayerSightRange(player, entityMoveToRet.missEntities, VisionContext.MISS);
            entitiesEnterPlayerSightRange(player, entityMoveToRet.meetEntities, VisionContext.MEET);

        } while (false);

        CheckRegionParam checkParam = new CheckRegionParam(CheckRegionType.CHECK_REGION_MOVE);
        checkParam.prevPos = prev_pos;
        checkParam.prevRoom = entity.getRoomId();                 // entityMoveTo目前不会改变room_id
        checkRegion(entity, ProtoScene.VisionType.VISION_NONE, checkParam);
//        checkEnterWeatherArea(entity, proto::VISION_NONE, checkParam);
    }


    // 检查实体是否进出区域
    public void checkRegion(Entity entity, ProtoScene.VisionType vision_type, CheckRegionParam check_param) {
        if (!entity.canEnterRegion()) {
            return;
        }

//        bool is_trigger_enter_leave_evt = Region::isTriggerEnterLeaveEvtByVisionType(vision_type);
//
//        SceneSightComp sight_comp = getSightComp();
//    Vector3 cur_pos = entity.getPosition();
//        set<Region> region_set = sight_comp.findPossibleRegionSet(cur_pos, entity.getRoomId());
//        if (check_param.type == CHECK_REGION_MOVE)
//        {
//            // 格子变动
//            if (sight_comp.isEntityMoveGrid(entity, check_param.prevPos, check_param.prevRoom))
//            {
//                set<Region> prev_region_set = sight_comp.findPossibleRegionSet(check_param.prevPos, check_param.prevRoom);
//                for (auto region : prev_region_set)
//                {
//                    region_set.insert(region);
//                }
//            }
//        }
//        for (Region region : region_set)
//        {
//            if (!region.isOnScene())
//            {
//                log.debug( + "[REGION] region is not on scene. " + *region + entity + " trigger_evt:" + is_trigger_enter_leave_evt;
//                continue;
//            }
//            switch (check_param.type)
//            {
//                case CHECK_REGION_MOVE:
//                {
//                    IntersectType intersect_type = region.getIntersectType(check_param.prevPos, cur_pos);
//                    if (intersect_type == INTERSECT_INWARD || intersect_type == INTERSECT_CROSS)
//                    {
//                        log.debug( + "[REGION] entity is entering region" + *region + entity + " trigger_evt:" + is_trigger_enter_leave_evt;
//                        region.addEntity(entity, is_trigger_enter_leave_evt);
//                    }
//                    if (intersect_type == INTERSECT_OUTWARD || intersect_type == INTERSECT_CROSS)
//                    {
//                        if (!region.isOnScene())
//                        {
//                            log.debug( + "[REGION] region is not on scene. " + *region + entity + " trigger_evt:" + is_trigger_enter_leave_evt;
//                        }
//                        else
//                        {
//                            log.debug( + "[REGION] entity is exiting region" + *region + entity + " trigger_evt:" + is_trigger_enter_leave_evt;
//                            region.delEntity(entity, is_trigger_enter_leave_evt);
//                        }
//                    }
//                    break;
//                }
//                case CHECK_REGION_BORN:
//                    if (region.isInRegion(cur_pos))
//                    {
//                        log.debug( + "[REGION] entity is born in region" + *region + entity + " trigger_evt:" + is_trigger_enter_leave_evt;
//                        region.addEntity(entity, is_trigger_enter_leave_evt);
//                    }
//                    break;
//                case CHECK_REGION_LEAVE:
//                    if (region.isInRegion(cur_pos))
//                    {
//                        log.debug( + "[REGION] entity is leave region" + *region + entity + " trigger_evt:" + is_trigger_enter_leave_evt;
//                        region.delEntity(entity, is_trigger_enter_leave_evt);
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
    }

    // 添加角色和武器的实体
    public void addAvatarAndWeaponEntity(Avatar avatar, boolean enterScene) {
        int avatarEntityId = avatar.getEntityId();
        Scene scene = avatar.getScene();
        // 当不在当前场景或无entity_id时分配
        if (scene == null || scene.getSceneId() != getSceneId() || avatarEntityId == 0) {
            avatarEntityId = genNewEntityId(ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR);
            avatar.setEntityId(avatarEntityId);
            // 先加entity后初始化ability 保证initAbility的时候玩家在场景内
            addGridlessEntity(avatar);
//            if (0 != avatar.initAbility(*this, true))
//            {
//                log._WARNING + "initAbility fails" + avatar;
//                return -1;
//            }
        }
//        WeaponGadget weapon_gadget = avatar.getEquipComp().getWeaponGadget();
//        if (weapon_gadget != null)
//        {
//            Scene weapon_scene = weapon_gadget.getScene();
//            if (weapon_scene == null || weapon_scene.getSceneId() != getSceneId() || weapon_gadget.getEntityId() == 0)
//            {
//                int weapon_entity_id = genNewEntityId(proto::PROT_ENTITY_WEAPON);
//                weapon_gadget.setEntityId(weapon_entity_id);
//                if (0 != weapon_gadget.initAbility(true))
//                {
//                    log._WARNING + "initAbility fails" + *weapon_gadget;
//                    return -1;
//                }
//                if (0 != addGridlessEntity(*weapon_gadget))
//                {
//                    log._WARNING + "addGridlessEntity fails, weapon_entity_id:" + weapon_entity_id + *weapon_gadget;
//                    return -1;
//                }
//                // 武器原来没EntityID.变为有EntityID 推送一下
//                avatar.getEquipComp().equipChangeNotify(EQUIP_WEAPON);
//            }
//        }
    }

    // 删除角色和武器实体
    public void delAvatarAndWeaponEntity(Avatar avatar) {
        if (avatar.isOnScene()) {
            log.error("avatar is on scene. " + avatar);
        }
        // 退出场景时，将entity_id置0
        int avatarEntityId = avatar.getEntityId();
        if (avatarEntityId == 0) {
            throw new UnknownLogicException("avatarEntityId == 0");
        }
//        avatar.getAbilityComp().clearAbilityComp();
//
//        avatar.onRemoveFromSceneTeam();   // onRemoveFromSceneTeam会调用delAllTeamBuffFromAvatar删除所有TeamBuff；回场景会走recover恢复
        avatar.setEntityId(0);
        avatar.setLastMoveSceneTimeMs(0);
//        avatar.getAnimatorComp().clearRendererChangedInfo();
        delGridlessEntity(avatarEntityId);

//        WeaponGadget weapon_gadget = avatar.getEquipComp().getWeaponGadget();
//        if (weapon_gadget != null)
//        {
//            if (weapon_gadget.isOnScene())
//            {
//                log._ERROR + "weapon_gadget is on scene. " + *weapon_gadget + " " + avatar;
//            }
//            int weapon_entity_id = weapon_gadget.getEntityId();
//            weapon_gadget.setEntityId(0);
//            weapon_gadget.getAbilityComp().clearAbilityComp();
//            weapon_gadget.getAnimatorComp().clearRendererChangedInfo();
//            if (0 != delGridlessEntity(weapon_entity_id))
//            {
//                log._WARNING + "delGridlessEntity fails, weapon_entity_id:" + weapon_entity_id + avatar;
//                ret = -1;
//            }
//        }
    }

    // 添加角色队伍实体
    public void addAvatarTeamEntity(Player player) {
        AvatarTeamEntity teamEntity = EntityMgr.createAvatarTeamEntity(this);
        addGridlessEntity(teamEntity);
        teamEntity.setScene(this);
        teamEntity.setPlayer(player);
//        teamEntity.setAuthorityPeerId(getPeerId(player.getUid()));
        player.getAvatarModule().setTeamEntity(teamEntity);
    }

    // 删除角色队伍实体
    public void delAvatarTeamEntity(Player player) {
        AvatarTeamEntity teamEntity = player.getAvatarModule().getTeamEntity();
        if (teamEntity == null) {
            throw new UnknownLogicException("teamEntity is null" + player);
        }
        // 退出场景时，删除队伍实体
        int entityId = teamEntity.getEntityId();
        player.getAvatarModule().setTeamEntity(null);
        delGridlessEntity(entityId);
//        notifyDelTeamEntity(entityId);
    }

    // 添加不在格子上的实体
    public void addGridlessEntity(Entity entity) {
        entity.setScene(this);
        if (gridlessEntityMap.put(entity.getEntityId(), entity) != null) {
            throw new UnknownLogicException("duplicate gridless entity_id:" + entity.getEntityId() + entity);
        }
    }

    // 删除不在格子上的实体
    public void delGridlessEntity(int entityId) {
        Entity entity = gridlessEntityMap.remove(entityId);
        if (entity == null) {
            throw new UnknownLogicException("can't find entityId " + entityId);
        }
        if (entity.isOnScene()) {
            log.error("entity is on scene. " + entity);
        }
        entity.setScene(null);
        gridlessEntityMap.remove(entityId);
    }

    // 查找实体
    public Entity findEntity(int entityId) {
        if (entityId == EntityUtils.LEVEL_RUNTIMEID) {
            return getSceneEntity();
        }
        Entity result = entityMap.get(entityId);
        if (result != null) {
            return result;
        }
        result = gridlessEntityMap.get(entityId);
        return result;
    }

    // 查找实体
    public Entity findEntityWithDead(int entityId) {
        Entity entity = findEntity(entityId);
        if (null != entity) {
            return entity;
        }
        return deadEntityMap.get(entityId);
    }

    // 添加死亡实体
    public void addDeadEntity(Entity entity, long deadClearTime) {
        log.debug("add dead entity_id:" + entity.getEntityId() + " desc:" + entity);
        entity.setDeadClearTime(TimeUtils.currentTimeMillis() + deadClearTime);
        if (deadEntityMap.put(entity.getEntityId(), entity) != null) {
            log.warn("duplicate dead entity_id:" + entity.getEntityId() + entity);
        }
    }

    // 定时清理死亡实体
    public void clearDeadEntity(long now) {
        if (deadEntityMap.isEmpty()) {
            return;
        }
        Iterator<Entity> iterator = deadEntityMap.values().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity.getDeadClearTime() < now) {
                iterator.remove();
            }
        }
    }

    // Entity是否存在在场景中
    public boolean isEntityExist(int entityId) {
        return findEntity(entityId) != null;
    }

    public boolean isEntityExist(Entity entity) {
        return isEntityExist(entity.getEntityId());
    }

    // 删除实体
    public void delEntity(int entity_id, VisionContext context) {
        Entity entity = findEntity(entity_id);
        if (entity == null) {
            throw new UnknownLogicException("can't find entityId " + entity_id);
        }

        try {
            entity.leaveScene(context, 0);
        } catch (Exception e) {
            log.warn("entity leaveScene fails" + entity);
            entityMap.remove(entity_id);
            throw e;
        }
    }

    // （单个）实体进入（单个）玩家视野范围
    public void entityEnterPlayerSightRange(Player player, Entity entity, VisionContext context) {
        Collection<Entity> realEnterList = new ArrayList<>();

        PlayerViewMgr viewMgr = findPlayerViewMgr(player.getUid());
        if (viewMgr == null) {
            throw new UnknownLogicException("viewMgr is null. uid:" + player.getUid());
        }

        viewMgr.addEntityInSightRange(entity);

        EntitySightGroup sg = entity.getSightGroup();
        if (sg == null) {
            if (viewMgr.isContainEntityInView(entity)) {
                return;
            }
            realEnterList.add(entity);
        } else {
            boolean groupVisionTypeClosed = SceneUtil.isGroupVisionTypeClosed();
            if (!groupVisionTypeClosed) {
                ProtoScene.GroupVisionType sgGroupVisionType = sg.getGroupVisionType();
                if (sgGroupVisionType != ProtoScene.GroupVisionType.GVT_ALWAYS_SHOW
                        && !viewMgr.isContainGroupVisionType(sgGroupVisionType.getNumber())) {
//#ifdef HK4Edebug
                    log.debug("sg:" + sg.getSelfId() + " group_vision_type:" + sgGroupVisionType + " not match with player");
//#endif
                    return;
                }
            }

            realEnterList = sg.getEntitiesInSightGroup();

            viewMgr.addSightGroupInView(sg);
            if (viewMgr.getChangeEyePropGuardState() == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE) {
                sg.onEnterPlayerView(player);
            }
//#ifdef HK4Edebug
            log.debug(" group_entity_vec size:" + realEnterList.size());
//#endif
        }

        notifyEntityAppear(player, realEnterList, context);
    }

    // （单个）实体离开（单个）玩家视野范围
    public void entityLeavePlayerSightRange(Player player, Entity entity, VisionContext context) {
        Collection<Entity> realLeaveList = new ArrayList<>();

        PlayerViewMgr viewMgr = findPlayerViewMgr(player.getUid());
        if (viewMgr == null) {
            throw new UnknownLogicException("viewMgr is null. uid:" + player.getUid());
        }

        viewMgr.delEntityInSightRange(entity);

        EntitySightGroup sg = entity.getSightGroup();
        if (sg == null) {
            if (!viewMgr.isContainEntityInView(entity)) {
                return;
            }
            realLeaveList.add(entity);
        } else {
            boolean allLeave = true;
            Collection<Entity> groupEntities = sg.getEntitiesInSightGroup();
            for (Entity groupEntity : groupEntities) {
                if (groupEntity == entity) {
                    continue;
                }
                if (viewMgr.isContainEntityInSightRange(groupEntity)) {
                    allLeave = false;
                    break;
                }
            }
            if (!allLeave) {
                return;
            }
            realLeaveList = groupEntities;
            viewMgr.delSightGroupInView(sg);
            if (viewMgr.getChangeEyePropGuardState() == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE) {
                sg.onExitPlayerView(player);
            }

//#ifdef HK4Edebug(
            log.debug("groupEntities size:" + groupEntities.size() + " realLeaveList size:" + realLeaveList.size());
//#endif
        }

        notifyEntityDisappear(player, realLeaveList, context);
    }

    // （多个）实体进入（单个）玩家视野范围
    public void entitiesEnterPlayerSightRange(Player player, Collection<Entity> entities, VisionContext context) {
        Collection<Entity> realEnterList = new ArrayList<>();

        PlayerViewMgr viewMgr = findPlayerViewMgr(player.getUid());
        if (viewMgr == null) {
            throw new UnknownLogicException("viewMgr is null. uid:" + player.getUid());
        }

        Set<EntitySightGroup> processedSightGroupSet = new HashSet<>();
        boolean groupVisionTypeClosed = SceneUtil.isGroupVisionTypeClosed();
        for (Entity entity : entities) {
            viewMgr.addEntityInSightRange(entity);

            EntitySightGroup sg = entity.getSightGroup();
            if (sg == null) {
                if (!viewMgr.isContainEntityInView(entity)) {
                    realEnterList.add(entity);
                }
            } else {
                if (!processedSightGroupSet.add(sg)) {
                    continue;
                }
                if (!groupVisionTypeClosed) {
                    ProtoScene.GroupVisionType sgGroupVisionType = sg.getGroupVisionType();
                    if (sgGroupVisionType != ProtoScene.GroupVisionType.GVT_ALWAYS_SHOW
                            && !viewMgr.isContainGroupVisionType(sgGroupVisionType.getNumber())) {
//#ifdef HK4Edebug(
                        log.debug("sg:" + sg.getSelfId() + " group_vision_type:" + sgGroupVisionType + " not match with player");
//#endif
                        continue;
                    }
                }

                Collection<Entity> groupEntities = sg.getEntitiesInSightGroup();
                realEnterList.addAll(groupEntities);

                viewMgr.addSightGroupInView(sg);
                if (viewMgr.getChangeEyePropGuardState() == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE) {
                    sg.onEnterPlayerView(player);
                }
//#ifdef HK4Edebug(
                log.debug(" group_entity_vec size:" + groupEntities.size());
//#endif
            }
        }

        notifyEntityAppear(player, realEnterList, context);
    }

    // （多个）实体离开（单个）玩家视野范围
    public void entitiesLeavePlayerSightRange(Player player, Collection<Entity> entities, VisionContext context) {
        Collection<Entity> realLeaveList = new ArrayList<>();

        PlayerViewMgr viewMgr = findPlayerViewMgr(player.getUid());
        if (viewMgr == null) {
            throw new UnknownLogicException("viewMgr is null. uid:" + player.getUid());
        }

        Set<Integer> entityIdSet = new HashSet<>();
        Set<EntitySightGroup> processedSightGroupSet = new HashSet<>();

        for (Entity entity : entities) {
            entityIdSet.add(entity.getEntityId());
            viewMgr.delEntityInSightRange(entity);
        }

        for (Entity entity : entities) {
            EntitySightGroup sg = entity.getSightGroup();
            if (sg == null) {
                if (viewMgr.isContainEntityInView(entity)) {
                    realLeaveList.add(entity);
                }
            } else {
                if (!processedSightGroupSet.add(sg)) {
                    continue;
                }
                boolean allLeave = true;
                Collection<Entity> groupEntities = sg.getEntitiesInSightGroup();
                for (Entity groupEntity : groupEntities) {
                    if (entityIdSet.contains(groupEntity.getEntityId())) {
                        continue;
                    }
                    if (viewMgr.isContainEntityInSightRange(groupEntity)) {
                        allLeave = false;
                        break;
                    }
                }
                if (!allLeave) {
                    continue;
                }
                realLeaveList.addAll(groupEntities);

                viewMgr.delSightGroupInView(sg);
                if (viewMgr.getChangeEyePropGuardState() == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE) {
                    sg.onExitPlayerView(player);
                }
//#ifdef HK4Edebug(
                log.debug(" groupEntities size:" + groupEntities.size() + " real_leave_vec size:" + realLeaveList.size());
//#endif
            }
        }

        notifyEntityDisappear(player, realLeaveList, context);
    }

    // （单个）实体进入（多个）玩家视野范围
    public void entityEnterPlayersSightRange(Collection<Long> uids, Entity entity, VisionContext context) {
//#ifdef HK4Edebug(
        log.debug("notify entity appear,entity_id:" + entity.getEntityId() + "uid_list:" + uids
                + "only_vision_uid:" + entity.getOnlyVisionUid() + "is_client_version:" + entity.isClientVisible());
//#endif

        for (Long uid : uids) {
            Player player = findPlayer(uid);
            if (player != null)
                entityEnterPlayerSightRange(player, entity, context);
        }
    }

    // （单个）实体离开（多个）玩家视野范围
    public void entityLeavePlayersSightRange(Collection<Long> uidList, Entity entity, VisionContext context) {
        log.debug("notify entity disappear,entity_id:" + entity.getEntityId() + "uid_list:" + uidList
                + "only_vision_uid:" + entity.getOnlyVisionUid() + "isClientVisible:" + entity.isClientVisible());

        for (Long uid : uidList) {
            Player player = findPlayer(uid);
            if (player != null)
                entityLeavePlayerSightRange(player, entity, context);
        }
    }

    // 通知实体(s)出现
    void notifyEntityAppear(Player player, Collection<Entity> entities, VisionContext context) {
        if (entities.isEmpty()) {
            return;
        }

        PlayerViewMgr view_mgr = findPlayerViewMgr(player.getUid());
        if (view_mgr == null) {
            throw new UnknownLogicException("viewMgr is null. uid:" + player.getUid());
        }

        boolean groupVisionTypeClosed = SceneUtil.isGroupVisionTypeClosed();
        PlayerViewMgr.PlayerChangeEyePropGuardState playerViewSyncGuardState = view_mgr.getChangeEyePropGuardState();
        if (playerViewSyncGuardState == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_GUARDING) {
            for (Entity entity : entities) {
                if (!entity.isClientVisible()) {
                    continue;
                }
                long onlyVisionUid = entity.getOnlyVisionUid();
                if (onlyVisionUid != 0 && onlyVisionUid != player.getUid()) {
                    continue;
                }
                if (!groupVisionTypeClosed) {
                    ProtoScene.GroupVisionType entityGroupVisionType = entity.getGroupVisionType();
                    if (entityGroupVisionType != ProtoScene.GroupVisionType.GVT_ALWAYS_SHOW
                            && !view_mgr.isContainGroupVisionType(entityGroupVisionType.getNumber())) {
//#ifdef HK4Edebug(
                        log.debug("entity:" + entity.getEntityId() + " group_vision_type:" + entityGroupVisionType + " not match with player");
//#endif
                        continue;
                    }
                }

                view_mgr.addEntityInView(entity);
            }
        } else {
            Collection<Entity> sendEntityList = new ArrayList<>(entities.size());
            for (Entity entity : entities) {
                if (!entity.isClientVisible()) {
                    continue;
                }
                long onlyVisionUid = entity.getOnlyVisionUid();
                if (onlyVisionUid != 0 && onlyVisionUid != player.getUid()) {
                    continue;
                }
                if (!groupVisionTypeClosed) {
                    ProtoScene.GroupVisionType entityGroupVisionType = entity.getGroupVisionType();
                    if (entityGroupVisionType != ProtoScene.GroupVisionType.GVT_ALWAYS_SHOW
                            && !view_mgr.isContainGroupVisionType(entityGroupVisionType.getNumber())) {
//#ifdef HK4Edebug(
                        log.debug("entity:" + entity.getEntityId() + " group_vision_type:" + entityGroupVisionType + " not match with player");
//#endif
                        continue;
                    }
                }

//                log.debug("playerViewSyncGuardState " + playerViewSyncGuardState);
                if (playerViewSyncGuardState == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE
                        && !view_mgr.addEntityInView(entity)) {
                    continue;
                }

                entity.onEnterPlayerView(player);

                if (entity.isClientCreated() && context.excludeUid == player.getUid()) {
                    continue;
                }
                sendEntityList.add(entity);
            }

            sendNotifyEntityAppear(player, sendEntityList, context);
        }
    }

    // 发送通知实体(s)出现
    void sendNotifyEntityAppear(Player player, Collection<Entity> entities, VisionContext context) {
        if (context.type == ProtoScene.VisionType.VISION_REPLACE_NO_NOTIFY) {
            return;
        }
        if (entities.isEmpty()) {
            return;
        }

//#ifdef HK4Edebug(
        StringBuilder sb = new StringBuilder();
        for (Entity entity : entities) {
            sb.append(entity.getEntityId()).append(',');
        }
        log.debug("send notify appear, entity_list:{" + sb + "} to uid:" + player.getUid());
//#endif

        Collection<Entity> movableEntityList = new ArrayList<>();
        ProtoScene.SceneEntityAppearNtf.Builder appearNtf = ProtoScene.SceneEntityAppearNtf.newBuilder();
        for (Entity entity : entities) {
            entity.toClient(appearNtf.addEntityListBuilder());

            switch (entity.getEntityType()) {
                case PROT_ENTITY_AVATAR: {
                    if (entity.getPlayerUid() == player.getUid()) {
                        log.debug("[VIEW] notify self avatar appear. uid:" + player.getUid() + " entity_id:" + entity.getEntityId());
                    }
                    movableEntityList.add(entity);
                }
                break;
                case PROT_ENTITY_MONSTER:
                    movableEntityList.add(entity);
                    break;
                default:
                    break;
            }
        }

        appearNtf.setAppearType(context.type);
        appearNtf.setParam(context.param);
        player.sendMessage(ProtoMsgId.MsgId.SCENE_ENTITY_APPEAR_NTF, appearNtf.build());

        // 通知上一次的移动信息
        for (Entity entity : movableEntityList) {
            entity.notifyLastMotionInfo(player);
        }
    }

    // 通知实体(s)消失
    void notifyEntityDisappear(Player player, Collection<Entity> entities, VisionContext context) {
        if (entities.isEmpty()) {
            return;
        }

        PlayerViewMgr view_mgr = findPlayerViewMgr(player.getUid());
        if (view_mgr == null) {
            throw new UnknownLogicException("viewMgr is null. uid:" + player.getUid());
        }

        PlayerViewMgr.PlayerChangeEyePropGuardState player_view_sync_guard_state = view_mgr.getChangeEyePropGuardState();
        if (player_view_sync_guard_state == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_GUARDING) {
            for (Entity entity : entities) {
                view_mgr.delEntityInView(entity);
            }
        } else {
            Collection<Integer> entityIdList = new ArrayList<>(entities.size());
            for (Entity entity : entities) {
                if (player_view_sync_guard_state == PlayerViewMgr.PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE
                        && !view_mgr.delEntityInView(entity)) {
                    continue;
                }
                entity.onExitPlayerView(player);
                entityIdList.add(entity.getEntityId());
            }

            sendNotifyEntityDisappear(player, entityIdList, context);
        }
    }

    // 通知实体(s)消失（只有发送逻辑）
    void sendNotifyEntityDisappear(Player player, Collection<Integer> entityIdList, VisionContext context) {
        if (context.type == ProtoScene.VisionType.VISION_REPLACE_NO_NOTIFY) {
            return;
        }
        if (entityIdList.isEmpty()) {
            return;
        }

//#ifdef HK4Edebug(
        log.debug("send disappear notify, entity_list:" + entityIdList + " to uid:" + player.getUid());
//#endif

        ProtoScene.SceneEntityDisappearNtf.Builder notify = ProtoScene.SceneEntityDisappearNtf.newBuilder();
        notify.addAllEntityList(entityIdList);
        notify.setDisappearType(context.type);
        player.sendMessage(ProtoMsgId.MsgId.SCENE_ENTITY_DISAPPEAR_NTF, notify.build());
    }

    // 重算玩家当前视野范围的实体并进入
    void refreshPlayerInSightRangeEntitiesAndEnter(Player player) {
        Entity playerEyeEntity = getPlayerEyeEntity(player.getUid());
        if (playerEyeEntity == null) {
            throw new UnknownLogicException("playerEyeEntity is null" + " uid:" + player.getUid());
        }

        PlayerViewMgr player_view_mgr = findPlayerViewMgr(player.getUid());
        if (player_view_mgr == null) {
            throw new UnknownLogicException("player_view_mgr is null" + " uid:" + player.getUid());
        }

        player_view_mgr.clearViewContainer();

        VisitEntityVisitor visitor = new VisitEntityVisitor(playerEyeEntity);
        getSightModule().visitGridsInSight(playerEyeEntity, visitor);
        List<Entity> meetEntityList = visitor.getResultList();
        Avatar avatar = player.getCurAvatar();
        if (avatar != null && avatar.isOnScene()) {
            meetEntityList.add(avatar);
        }
        entitiesEnterPlayerSightRange(player, meetEntityList, VisionContext.MEET);
    }

    public void notifyViewingPlayersExclude(Entity entity, ProtoMsgId.MsgId msgId, Message msg, long excludeUid, boolean includeMe) {
        Collection<Long> uidList = getViewingPlayers(entity, excludeUid, includeMe);
        for (Long uid : uidList) {
            Player player = findPlayer(uid);
            player.sendMessage(msgId, msg);
        }
//        GAME_THREAD_LOCAL.player_mgr.sendToPlayerList(uid_vec, msg);
    }

    // 获取正在查看该entity的玩家
    public Set<Long> getViewingPlayers(Entity entity, long excludeUid, boolean includeMe) {
        if (!entity.isOnScene()) {
            log.debug("entity is not on scene" + entity);
            return Set.of();
        }

        Collection<Player> viewingPlayers = entity.getViewMgr().getViewingPlayers();
        Set<Long> uidSet = new HashSet<>(viewingPlayers.size());
        for (Player player : viewingPlayers) {
            long uid = player.getUid();
            if (uid == excludeUid) {
                continue;
            }
            uidSet.add(uid);
        }

        long self_uid = entity.getPlayerUid();
        if (self_uid != 0) {
            if (includeMe && self_uid != excludeUid) {
                uidSet.add(self_uid);
            } else {
                uidSet.remove(self_uid);
            }
        }
        return uidSet;
    }

    private void createSceneEntity() {
        sceneEntity = createSceneEntityWithoutInitConfig();
//        SceneExcelConfig* scene_config = getConfig();
//        if (scene_config == nullptr)
//        {
//            LOG_WARNING + "getConfig fails";
//            return -1;
//        }
//        if (!scene_config->level_entity_config.empty())
//        {
//            scene_entity_->addInitLevelConfig(scene_config->level_entity_config);
//        }
    }

    private SceneEntity createSceneEntityWithoutInitConfig() {
        return EntityMgr.createSceneEntity(this);
    }

    private ProtoScene.ChangeSceneTeamReason getChangeSceneTeamReason() {
        return ProtoScene.ChangeSceneTeamReason.CHANGE_SCENE_TEAM_REASON_ENTER_SCENE;
    }

    public SceneTeam findSceneTeam() {
        World world = getOwnWorld();
        if (world == null) {
            return null;
        }
        return world.getSceneTeam();
    }

    public int processEntityMoveInfo(Player player, Avatar avatar, ProtoScene.EntityMoveInfo moveInfo) {
        boolean isCurAvatar = false;
        int entityId = moveInfo.getEntityId();
        Entity entity = findEntity(entityId);
        if (entityId == avatar.getEntityId()) {
            if (avatar.getLifeState() != ProtoCommon.LifeState.LIFE_ALIVE) {
                log.debug("avatar is not alive:" + avatar);
                return -1;
            }
            isCurAvatar = true;
        }
        if (null == entity) {
            log.debug("entity is null. entityId:" + entityId);
            return -1;
        }
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR && !isCurAvatar) {
            // TODO: 感觉可以和isOnScene换一下顺序
            Player entityPlayer = entity.getPlayer();
            if (entityPlayer != null && entityPlayer.getUid() != player.getUid()) {
                log.debug("can't control other player's avatar!" + entity + avatar);
                return -1;
            }
            // 处理跌落伤害的时序相反的特殊情况
            if (moveInfo.getMotionInfo().getState() != ProtoScene.MotionState.MOTION_LAND_SPEED) {
                return -1;
            }
            // 跳过onScene检查
        } else if (!entity.isOnScene()) {
            log.debug("entity is not on scene. entity:" + entity);
            return -1;
        }
        Entity.MotionContext motionContext = new Entity.MotionContext();
        motionContext.setSceneTimeMs(moveInfo.getMotionInfo().getSceneTime());
        motionContext.setExcludeUid(player.getUid());
        motionContext.setNotify(true);
        entity.setMotionInfo(moveInfo.getMotionInfo(), motionContext);
        return 0;
    }
}
