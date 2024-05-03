package com.ares.game.player;

import com.ares.common.math.Transform;
import com.ares.common.math.Vector3;
import com.ares.core.excetion.LogicException;
import com.ares.core.excetion.UnknownLogicException;
import com.ares.core.utils.RandomUtils;
import com.ares.game.DO.RoleDO;
import com.ares.game.network.PeerConn;
import com.ares.game.player.modules.avatar.PlayerAvatarModule;
import com.ares.game.player.modules.basic.PlayerBasicModule;
import com.ares.game.player.modules.item.PlayerItemModule;
import com.ares.game.player.modules.scene.PlayerSceneModule;
import com.ares.game.scene.PlayerViewMgr;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneUtil;
import com.ares.game.scene.VisionContext;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Getter
@Setter
@Slf4j
public class Player extends PlayerModuleContainer {
    private final long uid;
    private final PeerConn peerConn;
    private boolean reLogin;
    private LoginState loginState = LoginState.UnLogin;

    enum LoginState
    {
        UnLogin,//未登录
        Login,// 已登录
        Logout,//登出
    }

    private final PlayerBasicModule basicModule = new PlayerBasicModule(this);
    private final PlayerSceneModule sceneModule = new PlayerSceneModule(this);
    private final PlayerItemModule itemModule = new PlayerItemModule(this);
    private final PlayerAvatarModule avatarModule = new PlayerAvatarModule(this);

    public Player(long id, PeerConn peerConn) {
        this.uid = id;
        this.peerConn = peerConn;
    }

    /**
     * 登录
     * newPlayer: true,第一次创建玩家数据需要初始化操作
     * reLogin: true, player对象在内存中接到loginReq，false, 从db中load数据创建player对象
     * targetUid: 不为零, 表示要进入别人的大世界
     * clientReLogin: true, 客户端断线重连, false, 客户端重新登入
     **/
    public void login(boolean newPlayer, boolean reLogin, long targetUid, boolean clientReLogin)
    {
        log.info("uid:" + getUid() + " newPlayer:" + newPlayer + " reLogin:" + reLogin
                + " targetUid:" + targetUid + " clientReLogin:" + clientReLogin);

        this.reLogin = reLogin;

        // 执行每个组件的登录
        onLogin(newPlayer);

        loginState = LoginState.Login;

        // 判断是否需要通知客户端选角色
        if (0 == getAvatarModule().getAvatarCount())
        {
            // 通知客户端做新账号数据选择
//            proto::DoSetPlayerBornDataNotify notify;
//            sendProto(notify);
        }
        // 如果不需要，就直接准备进入场景
        else
        {
            // 重新登录不是退出联机
//            setIsQuitMp(false);
            // 初始化并开始进入场景
            initAndBeginEnterScene(reLogin, targetUid, clientReLogin);
        }
    }

    public Avatar getCurAvatar(){
        return null;
    }

    // 玩家初始化，然后通知进入场景
    private void initAndBeginEnterScene(boolean isReLogin, long targetUid, boolean isClientReLogin) {

        // 因为登录时可能是新账号，要在客户端选择角色之后推送数据，统一写在初始化场景之前
        notifyAllData();

        // target_uid: player跨进程迁移
        if (targetUid != 0 && targetUid != uid)
        {
            log.debug("[TRANSFER] initAndBeginEnterScene joinPlayerScene uid:" + getUid() + " target_uid:" + targetUid);
            int ret = sceneModule.joinPlayerScene(targetUid);
            if (0 == ret)
            {
                return;
            }

//            proto::JoinPlayerFailNotify notify;
//            notify.set_retcode(ret);
//            sendProto(notify);
        }

        // 判断玩家当前在哪个大世界
        PlayerWorld playerWorld = sceneModule.getCurPlayerWorld();
        if (playerWorld == null)
        {
            playerWorld = sceneModule.getMyPlayerWorld();
            if (playerWorld == null)
            {
                throw new UnknownLogicException("getMyPlayerWorld fails, uid: " + getUid());
            }
        }

        Scene scene = sceneModule.getCurScene();
        do
        {
            if (scene != null)
            {
                ProtoCommon.SceneType scene_type = scene.getSceneType();
                switch (scene_type)
                {
                    case SCENE_WORLD:
                    case SCENE_ROOM:
                        break;
                    case SCENE_DUNGEON:
                    {
//                        DungeonScene dungeonScene = dynamic_pointer_cast<DungeonScene>(scene);
//                        if (dungeonScene != null
//                                && dungeonScene.getDungeonResult() == DUNGEON_RESULT_NONE
//                                // 角色存活或者还有机会复活
//                                && (!avatar_comp.isAllAvatarDead() || avatar_comp.isRevivableAfterAllDead())
//                                // 副本的主机还在
//                                // STOP_CODE_CHECKER
//                                && dungeonScene.getOwnPlayer() != null)
//                        // RESTART_CODE_CHECKER
//                        {
//                            scene = dungeonScene;
//                            log.DEBUG + "[EnterScene] initScene is dungeonScene, uid: " + getUid();
//                        }
//                        else
//                        {
//                            scene = dungeon_comp.getQuitScene();
//                            log.DEBUG + "[EnterScene] initScene is dungeon_quitScene, uid:" + getUid();
//                            if (scene == null)
//                            {
//                                log.WARNING + "getQuitScenePosRot uid: " + getUid() + " failed, using PlayerWorldScene";
//                            }
//                        }
                        break;
                    }
                    default:
                    {
                        throw new UnknownLogicException("[EnterScene] Unknown scene type: " + scene_type+ " scene_id:" + scene.getSceneId() + " uid:" + getUid());
                    }
                }

                if (scene != null)
                {
                    break;
                }
            }

            int myCurPlayerSceneId = sceneModule.getMyCurSceneId();
            if (myCurPlayerSceneId == 0)
            {
                // 新用户，读取世界场景的配置
                myCurPlayerSceneId = playerWorld.getMainWorldSceneId();
            }
            if (myCurPlayerSceneId == 0)
            {
                throw new UnknownLogicException("can't find enter scene_id, uid: " + getUid());
            }
            scene = playerWorld.getScene(myCurPlayerSceneId);
            if (scene == null)
            {
                throw new UnknownLogicException("scene is null, scene_id: " + myCurPlayerSceneId + " uid: " + getUid());
            }
        }
        while (false);

        if (scene == null)
        {
            throw new UnknownLogicException("scene is null,uid: " + getUid());
        }
        
        beginEnterScene(scene, isReLogin, isClientReLogin);
    }


    // 开始进入场景
    private void beginEnterScene(Scene scene, boolean isReLogin, boolean isClientReLogin)
    {
        ProtoScene.EnterType enterType;
        ProtoCommon.SceneType sceneType = scene.getSceneType();
        switch (sceneType)
        {
            case SCENE_WORLD:
            case SCENE_ROOM:
                enterType = ProtoScene.EnterType.ENTER_SELF;
                break;
            case SCENE_DUNGEON:
                enterType = ProtoScene.EnterType.ENTER_DUNGEON;
                break;
            default:
                throw new UnknownLogicException("[EnterScene] Unknown scene type: " + sceneType);
        }

        Transform transform = null;
        Scene curScene = sceneModule.getCurScene();
        if (curScene != null && ProtoCommon.SceneType.SCENE_WORLD == scene.getSceneType()
                && getAvatarModule().isAllAvatarDead()
                && !getAvatarModule().isRevivableAfterAllDead())
        {
//            // 全队死亡不能在原地复活，否则存在作弊的风险
//            if (0 != avatar_comp.findRebornPositionRotation(false, scene, bornPos, bornRot))
//            {
//                log.WARNING + "findRebornPositionRotation uid: " + getUid() + " failed";
//                return -1;
//            }
//            log.DEBUG + "findRebornPositionRotation succ, uid:" + getUid();
        }
        else
        {
            transform = scene.getPlayerCurrentLocation(uid);
        }

        beginEnterScene(scene, transform, enterType, ProtoScene.VisionType.VISION_MEET, isReLogin, isClientReLogin, ProtoScene.EnterReason.ENTER_REASON_LOGIN);
    }

    void beginEnterScene(Scene destScene, Transform transform, ProtoScene.EnterType enterType,
                         ProtoScene.VisionType visionType, boolean isReLogin, boolean isClientReLogin, ProtoScene.EnterReason enterReason)
    {
        if (destScene == null)
        {
            throw new UnknownLogicException("destScene is null,uid: " + getUid());
        }

        PlayerSceneModule.EnterSceneState enterScene_state = sceneModule.getEnterSceneState();
        if (enterScene_state != PlayerSceneModule.EnterSceneState.ENTER_SCENE_NONE
                && enterScene_state != PlayerSceneModule.EnterSceneState.ENTER_SCENE_POST)
        {
            // 只是打印LOG，不阻塞
            log.info("beginEnterScene with last one not finish, uid:" + getUid());
        }

        // 记录传送目标
        markDestination(destScene, transform, enterType, visionType, isReLogin, isClientReLogin);
        // 发送进场景通知
        ProtoScene.PlayerEnterSceneNtf.Builder notify = ProtoScene.PlayerEnterSceneNtf.newBuilder();
        int oldToken = sceneModule.getEnterSceneToken();
        notify.setIsFirstLoginEnterScene(oldToken == 0);
        int enterSceneToken = oldToken == 0 ? RandomUtils.nextInt(10000) : oldToken + 100;
        sceneModule.setEnterSceneToken(enterSceneToken);
        notify.setEnterSceneToken(enterSceneToken);
        notify.setType(enterType);
        notify.setSceneId(destScene.getSceneId());
        transform.getPosition().toClient(notify.getPosBuilder());
        notify.setSceneBeginTime(destScene.getBeginTimeMs());
        notify.setTargetUid(destScene.getOwnerUid());
        notify.setIsSkipUi(enterReason == ProtoScene.EnterReason.ENTER_REASON_LUA_SKIP_UI);
        notify.setEnterReason(enterReason);

        Scene curScene = sceneModule.getCurScene();
        if (curScene != null)
        {
            notify.setPrevSceneId(curScene.getSceneId());
            curScene.getPlayerCurrentLocation(uid).getPosition().toClient(notify.getPrevPosBuilder());
        }
        ProtoCommon.SceneType destSceneSceneType = destScene.getSceneType();
        if (destSceneSceneType == ProtoCommon.SceneType.SCENE_DUNGEON)
        {
//            DungeonScene dungeonScene = std::dynamic_pointer_cast<DungeonScene>(destScene);
//            if (null != dungeonScene)
//            {
//                notify.set_dungeon_id(dungeonScene.getDungeonId());
//            }
        }
        else if (SceneUtil.isPlayerScene(destSceneSceneType))
        {
            PlayerWorld destWorld = destScene.getOwnPlayerWorld();
            if (destWorld != null)
            {
                notify.setWorldLevel(destWorld.getLevel());
            }
        }
//        sendProto(notify);

        sceneModule.setEnterSceneState(PlayerSceneModule.EnterSceneState.ENTER_SCENE_NOTIFY);

        log.info("[EnterScene] BeginEnterScene uid:" + getUid() + " enter_type:" + enterType
            + " token:" + notify.getEnterSceneToken() + " destScene_id:"
            + notify.getSceneId() + " pos:" + notify.getPos() + " target_uid:" + notify.getTargetUid());
    }

    public ProtoScene.EnterSceneReadyRes enterSceneReady(ProtoScene.EnterSceneReadyReq req)
    {
        // 进入场景的条件检查
        if (req.getEnterSceneToken() != sceneModule.getEnterSceneToken())
        {
            log.debug("[ENTER_SCENE] token not match, client_token:" + req.getEnterSceneToken() + " server_token:" + sceneModule.getEnterSceneToken());
            throw new LogicException(ProtoCommon.ErrCode.ENTER_SCENE_TOKEN_INVALID_VALUE);
        }
        if (sceneModule.getEnterSceneState() != PlayerSceneModule.EnterSceneState.ENTER_SCENE_NOTIFY)
        {
            log.debug("EnterSceneState is not EnterSceneNotify, uid:" + getUid());
            throw new UnknownLogicException("EnterSceneState is not EnterSceneNotify");
        }

        Scene destScene = sceneModule.getDestScene();
        if (destScene == null)
        {
            throw new UnknownLogicException("DestScene is null, uid:" + getUid());
        }

        // 从别人的大世界回到自己世界的场景
//        uint32_t cur_world_owner_uid = sceneModule.getCurWorldOwnerUid();
//        if (cur_world_owner_uid != 0 && cur_world_owner_uid != getUid()
//                && SceneExcelConfigMgr::isPlayerScene(destScene.getSceneType())
//            && destScene.getOwnerUid() == getUid())
//        {
//            log.DEBUG + "[WORLD] uid: " + getUid() + " quitMp and back to self world";
//            setIsQuitMp(true);
//        }

        // 预进入场景，会占用一个peer，但并没有真正进入，超时删除
        try {
            preEnterScene(destScene);
        }
        catch (Exception ex) {
            clearDestination();
            log.warn("preEnterScene failed, uid:" + getUid());
            if (ex instanceof LogicException) {
                throw (LogicException) ex;
            }
            throw new UnknownLogicException("preEnterScene failed, uid:" + getUid());
        }

        Scene curScene = sceneModule.getCurScene();
        if (curScene == destScene)
        {
            disappearInCurScene();
        }
        else if (curScene != null)
        {
            // 刚登陆的时候并不在任何场景内
            // 先从上一个场景退出
            leaveCurScene();
        }
        else if (sceneModule.getCurWorldOwnerUid() > 0 && sceneModule.getCurWorld() == null)
        {
            log.warn("leaveDestroyedWorld, uid: " + getUid());
            leaveDestroyedWorld();
        }

        // 如果是从联机退回来的，客户端需要回滚任务状态，直接通知客户端断线重连
//        if (getIsQuitMp())
//        {
//            log.DEBUG + "uid: " + getUid() + " quitMp and back to self world";
//            ScenePlayerLocation location;
//            location.cur_pos = sceneModule.getDestPos();
//            location.cur_rot = sceneModule.getDestRot();
//            location.last_valid_pos = location.cur_pos;
//            location.last_valid_rot = location.cur_rot;
//            destScene.setPlayerLocation(getUid(), location);
//
//            proto::ClientReconnectNotify notify;
//            notify.set_reason(proto::CLIENT_RECONNNECT_QUIT_MP);
//            sendProto(notify);
//            log.INFO + "[MP] QuitMp send ReconnectNotify, uid:" + getUid();
//
//            // 依然把状态设置成ENTER_SCENE_READY
//            sceneModule.setEnterSceneState(ENTER_SCENE_READY);
//
//            return 0;
//        }

        ProtoScene.EnterScenePeerNtf.Builder notify = ProtoScene.EnterScenePeerNtf.newBuilder();
        notify.setDestSceneId(destScene.getSceneId());
        notify.setPeerId(destScene.getPeerId(getUid()));
        notify.setHostPeerId(destScene.getHostPeerId());
        notify.setEnterSceneToken(sceneModule.getEnterSceneToken());
//        sendProto(notify);

        sceneModule.setEnterSceneState(PlayerSceneModule.EnterSceneState.ENTER_SCENE_READY);

        log.info("[EnterScene] EnterSceneReady uid:" + getUid() + " destScene_id:" + destScene.getSceneId()
                + " peer_id:" + destScene.getPeerId(getUid()) + " host_peer_id:" + destScene.getHostPeerId());

        return ProtoScene.EnterSceneReadyRes.newBuilder().setEnterSceneToken(req.getEnterSceneToken()).build();
    }

    void disappearInCurScene()
    {
        Scene curScene = sceneModule.getCurScene();
        if (curScene == null)
        {
            log.warn("[EnterScene] getCurScene failed, uid:" + getUid());
            return;
        }
//
//        Avatar cur_avatar = getCurAvatar();
//        if (cur_avatar == null)
//        {
//            log.DEBUG + "[EnterScene] getCurAvatar failed, uid:" + getUid();
//            return;
//        }
//
//        if (cur_avatar.getScene() == curScene)
//        {
//            cur_avatar.leaveScene();
//        }
//
//        PlayerViewMgr player_view_mgr = curScene.findPlayerViewMgr(getUid());
//        if (player_view_mgr != null)
//        {
//            player_view_mgr.resetPlayerViewMgr();
//        }
//        else
//        {
//            log.ERROR("uid:%u view_mgr is null", getUid());
//            // assert(0);
//        }
    }

    void leaveCurScene()
    {
        Scene curScene = sceneModule.getCurScene();
        if (curScene == null)
        {
            throw new UnknownLogicException("[EnterScene] getCurScene failed, uid:" + getUid());
        }

//        Avatar cur_avatar = getCurAvatar();
//        if (cur_avatar != null)
//        {
//            cur_avatar.leaveScene();
//        }
//        else
//        {
//            log.WARNING + "[EnterScene] getCurAvatar failed, uid:" + getUid();
//        }

        curScene.playerLeave(getUid());
    }

    void leaveDestroyedWorld()
    {
        // 由于主机销毁导致客机被动的离开，但是当前场景和世界是不存在的
        long ownerUid = sceneModule.getCurWorldOwnerUid();
        if (sceneModule.getCurWorld() != null || ownerUid == 0)
        {
            log.error("uid: " + getUid() + " getCurWorld() != null, ownerUid: " + ownerUid);
            return;
        }
        log.debug("enter dest scene, but cur scene is destroied, getCurWorldOwnerUid(): "
                + sceneModule.getCurWorldOwnerUid() + " uid: " + getUid());

        sceneModule.playerLeaveScene();

        sceneModule.setCurWorld(null);
        sceneModule.setCurWorldOwnerUid(0);

//        // 遍历所有角色 清理一下
//        std::vector<AvatarWtr> avatar_vec = getAvatarComp().getAllAvatarVec();
//        for (AvatarWtr avatar_wtr : avatar_vec)
//        {
//            Avatar avatar = avatar_wtr.lock();
//            if (avatar == null)
//            {
//                continue;
//            }
//            avatar.setGrid(null);
//        }
    }

    void preEnterScene(Scene destScene)
    {
        if (destScene == null)
        {
            throw new UnknownLogicException("[EnterScene] preEnterScene failed, uid:" + getUid());
        }

        World curWorld = sceneModule.getCurWorld();
        World destWorld = destScene.getOwnWorld();
        if (destWorld == null)
        {
            throw new UnknownLogicException("[EnterScene] destWorld is null, uid: " + getUid());
        }
        // 在大世界中先占据一个槽位(如果进入失败，最终会删除)
        if (curWorld != destWorld && destScene.getSceneType() != ProtoCommon.SceneType.SCENE_DUNGEON)
        {
            destWorld.playerPreEnter(this);
        }

        destScene.playerPreEnter(this);
    }

    private void markDestination(Scene destScene, Transform transform, ProtoScene.EnterType enterType,
                                 ProtoScene.VisionType visionType, boolean isReLogin, boolean isClientReLogin)
    {
        sceneModule.setInTransfer(true);
        sceneModule.setDestScene(destScene);
        sceneModule.setDestPos(transform.getPosition());
        sceneModule.setDestRot(transform.getRotation());
        sceneModule.setDestEnterType(enterType);
        sceneModule.setDestVisionType(visionType);
        sceneModule.setEnterReLogin(isReLogin);
        sceneModule.setClientReLogin(isClientReLogin);
    }

    public ProtoScene.SceneInitFinishRes sceneInitFinish(ProtoScene.SceneInitFinishReq req)
    {
        if (req.getEnterSceneToken() != sceneModule.getEnterSceneToken())
        {
            log.debug("[ENTER_SCENE] token not match, client_token:" + req.getEnterSceneToken() + " server_token:" + sceneModule.getEnterSceneToken());
            throw new LogicException(ProtoCommon.ErrCode.ENTER_SCENE_TOKEN_INVALID_VALUE);
        }

        // 进入场景的条件检查
        if (sceneModule.getEnterSceneState() != PlayerSceneModule.EnterSceneState.ENTER_SCENE_READY)
        {
            throw new UnknownLogicException("EnterSceneState is not EnterSceneReady, uid:" + getUid());
        }

        Scene destScene = sceneModule.getDestScene();
        Vector3 dest_pos = sceneModule.getDestPos();
        Vector3 dest_rot = sceneModule.getDestRot();
        ProtoScene.EnterType enterType = sceneModule.getDestEnterType();
        ProtoScene.VisionType visionType = sceneModule.getDestVisionType();
        boolean enterReLogin = sceneModule.isEnterReLogin();

        if (destScene == null)
        {
            throw new UnknownLogicException("getDestScene failed, uid:" + getUid());
        }

        World curWorld = sceneModule.getCurWorld();
        World destWorld = destScene.getOwnWorld();
        if (destWorld == null)
        {
            throw new UnknownLogicException("destWorld is null, uid: " + getUid());
        }

        // definite_world是后面流程走完后，玩家所在的确切的world, 先初始化为cur_world, 可能为null
        World definiteWorld = curWorld;
        if (curWorld != destWorld && destScene.getSceneType() != ProtoCommon.SceneType.SCENE_DUNGEON)
        {
            destWorld.playerEnter(this);
            definiteWorld = destWorld;
        }

        if (reLogin)
        {
            if (definiteWorld != null)
            {
                definiteWorld.notifyWorldData(this);
            }
        }

        // 1. 分配角色entity_id, 并通知客户端
        playerEnterScene(destScene, dest_pos, dest_rot, enterType, visionType, enterReLogin);

        sceneModule.setEnterSceneState(PlayerSceneModule.EnterSceneState.ENTER_SCENE_INIT);

        return ProtoScene.SceneInitFinishRes.newBuilder().setEnterSceneToken(req.getEnterSceneToken()).build();
    }

    void playerEnterScene(Scene destScene, Vector3 pos, Vector3 rot, ProtoScene.EnterType enter_type,
                             ProtoScene.VisionType vision_type, boolean isReLogin)
    {
        if (destScene == null)
        {
            throw new UnknownLogicException("[EnterScene] destScene is null, uid:" + getUid());
        }

        // 处理角色与队伍进入场景
        InitEnterSceneAvatarRet initEnterSceneAvatarRet = initEnterSceneAvatar(destScene);
//
//        DungeonScene dungeonScene = dynamic_pointer_cast<DungeonScene>(destScene);
//        if (dungeonScene != null && 0 != getDungeonComp().trySubDungeonTicket(dungeonScene))
//        {
//            // 尝试扣除副本门票
//            log.WARNING + "trySubDungeonTicket fails, dungeon_id:" + dungeonScene.getDungeonId();
//            return proto::RET_DUNGEON_TICKET_FAIL;
//        }

        destScene.playerEnter(this, pos, rot, isReLogin, initEnterSceneAvatarRet.enterSceneAvatarList, initEnterSceneAvatarRet.appearAvatar);

//        // 客户端要求，LifeState和血量，必须在PlayerEnterSceneInfoNotify之后发送
//        for (Avatar avatar : refresh_avatar_vec)
//        {
//            avatar.getFightPropComp().notifyProp(FIGHT_PROP_CUR_HP);
//            proto::AvatarLifeStateChangeNotify change_notify;
//            change_notify.set_avatar_guid(avatar.getGuid());
//            change_notify.set_life_state(avatar.getLifeState());
//            sendProto(change_notify);
//        }

//        // 客户端暂时无法处理登录时的地图标记通知，暂时改成每次进场景后全部通知一遍
//        sceneModule.notifyAllMarkPoint();
    }

    public ProtoScene.EnterSceneDoneRes enterSceneDone(ProtoScene.EnterSceneDoneReq req)
    {
        if (req.getEnterSceneToken() != sceneModule.getEnterSceneToken())
        {
            log.debug("[ENTER_SCENE] token not match, client_token:" + req.getEnterSceneToken() + " server_token:" + sceneModule.getEnterSceneToken());
            throw new LogicException(ProtoCommon.ErrCode.ENTER_SCENE_TOKEN_INVALID_VALUE);
        }
        if (sceneModule.getEnterSceneState() != PlayerSceneModule.EnterSceneState.ENTER_SCENE_INIT)
        {
            throw new UnknownLogicException("EnterSceneState is not EnterSceneInit, uid:" + getUid());
        }
        Scene curScene = sceneModule.getCurScene();
        if (curScene == null)
        {
            throw new UnknownLogicException("getCurScene is null, uid:" + getUid());
        }

        // 正式EnterSceneDone流程
        ProtoScene.VisionType visionType = sceneModule.getDestVisionType();
        Vector3 destPos = sceneModule.getDestPos();
        Vector3 destRot = sceneModule.getDestRot();

        // 完成进场流程，取消isReLogin设置
        this.reLogin = false;

        Avatar curAvatar = getCurAvatar();
        if (curAvatar == null)
        {
            throw new UnknownLogicException("[EnterScene] getCurAvatar failed, uid:" + getUid());
        }

        if (curAvatar.getLifeState() == ProtoCommon.LifeState.LIFE_DEAD)
        {
            log.warn("cur avatar is dead! curAvatar:" + curAvatar + " player:" + this);
            curAvatar.setLifeState(ProtoCommon.LifeState.LIFE_ALIVE);
            if (curAvatar.getCurHp() == 0)
            {
                curAvatar.setCurHp(1, false);
            }
        }

        curAvatar.setPosition(destPos);
        curAvatar.setRotation(destRot);
        curAvatar.setLastValidPos(destPos);
        curAvatar.setLastValidRot(destRot);
        curAvatar.clearSpeed();

        curScene.entityAppear(curAvatar, new VisionContext(visionType));

        PlayerViewMgr player_view_mgr = curScene.findPlayerViewMgr(getUid());
        if (player_view_mgr == null)
        {
            throw new UnknownLogicException("[EnterScene] player_view_mgr is null, uid:" + getUid());
        }
        player_view_mgr.notifyEyePointState();

        sceneModule.setEnterSceneState(PlayerSceneModule.EnterSceneState.ENTER_SCENE_DONE);

//        if (0 != curScene.getBlockGroupComp().notifyGroupSuiteToClient(*this))
//        {
//            log.WARNING + "notifyGroupSuiteToClient failed";
//        }

        log.info("[EnterScene] enterSceneDone succ, uid:" + getUid());

        return ProtoScene.EnterSceneDoneRes.newBuilder().setEnterSceneToken(req.getEnterSceneToken()).build();
    }

    public ProtoScene.PostEnterSceneRes postEnterScene(ProtoScene.PostEnterSceneReq req)
    {
        if (req.getEnterSceneToken() != sceneModule.getEnterSceneToken())
        {
            log.debug("[ENTER_SCENE] token not match, client_token:" + req.getEnterSceneToken() + " server_token:" + sceneModule.getEnterSceneToken());
            throw new LogicException(ProtoCommon.ErrCode.ENTER_SCENE_TOKEN_INVALID_VALUE);
        }
        if (sceneModule.getEnterSceneState() != PlayerSceneModule.EnterSceneState.ENTER_SCENE_DONE)
        {
            throw new UnknownLogicException("EnterSceneState is not EnterSceneDone, uid:" + getUid());
        }

        Scene curScene = sceneModule.getCurScene();
        if (curScene == null)
        {
            throw new UnknownLogicException("getCurScene is null, uid:" + getUid());
        }

        // 完成切场景流程
        ProtoScene.EnterType destEnterType = sceneModule.getDestEnterType();
        clearDestination();

        sceneModule.setEnterSceneState(PlayerSceneModule.EnterSceneState.ENTER_SCENE_POST);
        sceneModule.setInTransfer(false);

        // 更新联机情况
//        getMpComp().updateMpPlayerInfo();

        log.info("[EnterScene] postEnterScene succ, uid:" + getUid());

        // 一些进场后的通知
//        getQuestComp().tryNotifyAllBeginChapter();

        // 检查是否设置角色无敌
//        auto& avatar_comp = getAvatarComp();
//        if (avatar_comp.getIsAllDieEnterScene())
//        {
//            avatar_comp.setIsAllDieEnterScene(false);
//            avatar_comp.dieInvincible();
//        }

//        Player owner_player = curScene.getOwnPlayer();
//        if (owner_player != null)
//        {
//            // 通知主机有客机进入场景
//            BaseEvent event = MAKE_SHARED<PlayerEnterSceneEvent>(curScene.getSceneId(), getUid());
//            if (event != null && owner_player != null)
//            {
//                owner_player.getEventComp().notifyEvent(event);
//            }
//        }

        // 触发进入场景后的事件
        triggerEnterSceneEvent(curScene, destEnterType);

//        curScene.getMultistagePlayComp().onPostEnterScene(*this);

        return ProtoScene.PostEnterSceneRes.newBuilder().setEnterSceneToken(req.getEnterSceneToken()).build();
    }

    static class InitEnterSceneAvatarRet {
        List<Avatar> enterSceneAvatarList;
        Avatar appearAvatar;
        List<Avatar> refreshAvatarList;
    }

    InitEnterSceneAvatarRet initEnterSceneAvatar(Scene destScene)
    {
        return null;
    }

    void clearDestination()
    {
        sceneModule.setDestScene(null);
        sceneModule.setDestPos(Vector3.ZERO);
        sceneModule.setDestRot(Vector3.ZERO);
        sceneModule.setDestEnterType(ProtoScene.EnterType.ENTER_NONE);
        sceneModule.setDestVisionType(ProtoScene.VisionType.VISION_NONE);
        sceneModule.setEnterReLogin(false);
        sceneModule.setClientReLogin(false);
    }

    void triggerEnterSceneEvent(Scene scene, ProtoScene.EnterType enter_type)
    {
//        if (scene == null)
//        {
//            log.WARNING + "triggerEnterSceneEvent failed, uid:" + getUid();
//            return;
//        }
//
//        BaseEvent event = MAKE_SHARED<EnterSceneDoneEvent>(scene.getSceneId(), enter_type);
//        if (event != null)
//        {
//            getEventComp().notifyEvent(event);
//        }
//
//        if (scene.getOwnPlayer() == shared_from_this())
//        {
//            SceneType scene_type = scene.getSceneType();
//            BaseEvent event;
//            if (scene_type == SceneType::SCENE_DUNGEON)
//            {
//                DungeonScene dungeonScene = dynamic_pointer_cast<DungeonScene>(scene);
//                if (dungeonScene == null)
//                {
//                    log.WARNING + "dynamic_pointer_cast DungeonScene failed";
//                    return;
//                }
//                event = MAKE_SHARED<EnterDungeonEvent>(dungeonScene.getDungeonId(), enter_type);
//            }
//            else
//            {
//                if (scene_type == SceneType::SCENE_WORLD)
//                {
//                    event = MAKE_SHARED<EnterMyWorldEvent>(scene.getSceneId());
//                }
//                else if (scene_type == SceneType::SCENE_ROOM)
//                {
//                    event = MAKE_SHARED<EnterRoomEvent>(scene.getSceneId());
//                }
//            }
//            if (event == null)
//            {
//                log.WARNING + "create Event failed";
//                return;
//            }
//            getEventComp().notifyEvent(event);
//        }
    }

    public void notifyPlayerEnterSceneInfo()
    {
        Avatar curAvatar = getCurAvatar();
        if (curAvatar == null)
        {
            throw new UnknownLogicException("curAvatar is null, uid:" + getUid());
        }
        Scene scene = sceneModule.getCurScene();
        if (scene == null)
        {
            throw new UnknownLogicException("cur_scene is null, uid:" + getUid());
        }
//        PlayerAvatarComp& avatar_comp = getAvatarComp();
//        proto::PlayerEnterSceneInfoNotify notify;
//        notify.set_cur_avatar_entity_id(curAvatar.getEntityId());
//        notify.set_enterScene_token(sceneModule.getEnterSceneToken());
//        int32_t ret = getAvatarComp().foreachMyAvatarInSceneTeam([&notify](Avatar & avatar)
//            {
//                proto::AvatarEnterSceneInfo& avatar_enter_info = *notify.add_avatar_enter_info();
//                if (0 != avatar.toClient(avatar_enter_info))
//                {
//                    log.WARNING + "toClient fails" + avatar;
//                    return FOREACH_BREAK;
//                }
//                return FOREACH_CONTINUE;
//            });
//        AvatarTeamEntity team_entity = avatar_comp.getTeamEntity();
//        if (team_entity == null)
//        {
//            log.ERROR + "team_entity is null" + *this;
//            return -1;
//        }
//        proto::TeamEnterSceneInfo& team_enter_info = *notify.mutable_team_enter_info();
//        if (0 != team_entity.toClient(team_enter_info))
//        {
//            log.WARNING + "toClient fails" + *team_entity;
//            return -1;
//        }
//        MPLevelEntity mp_level_entity = scene.getOrCreateMPLevelEntity();
//        if (null == mp_level_entity)
//        {
//            log.WARNING + "create mp level entity failed";
//            return -1;
//        }
//        if (0 != mp_level_entity.toClient(*notify.mutable_mp_level_entity_info()))
//        {
//            log.WARNING + "mp level entity to client failed";
//            return -1;
//        }
//        sendProto(notify);
    }
    
    

    // 在登录线程执行
    public void fromBin(RoleDO roleDO) {
        try {
            modulesFromBin(BinServer.PlayerDataBin.parseFrom(roleDO.getBin()));
        }
        catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("parse bin error. uid:" + uid, e);
        }
    }

    public void toBin(RoleDO roleDO) {
        roleDO.setUid(uid);

        BinServer.PlayerDataBin.Builder builder = BinServer.PlayerDataBin.newBuilder();
        modulesToBin(builder);
        roleDO.setBin(builder.build().toByteArray());
    }

    // 在登录线程执行
    public void init() {
        modulesInit();
    }

    // 在逻辑线程执行
    // 在这里执行启动定时器等操作
    public void start() {
       // modulesStart();
    }

    // 在登录线程执行
    public void onFirstLogin() {
        modulesOnFirstLogin();
    }

    // 在逻辑线程执行
    public void onLogin(boolean isNewPlayer) {
        modulesOnLogin(isNewPlayer);
    }

    public void notifyAllData() {
        modulesNotifyAllData();
    }

    public void onDisconnect() {
        modulesOnDisconnect();
    }

    public void onReconnect() {
        modulesOnReconnect();
    }

    public void onLogout() {
        modulesOnLogout();
    }

    public void onDailyRefresh() {
        modulesOnDailyRefresh();
    }

    public void onLoginDailyRefresh() {
        modulesOnLoginDailyRefresh();
    }

    public void onLeaveScene() {
        modulesOnLeaveScene();
    }

    public void sendMessage(ProtoCommon.MsgId msgId, Message message) {
        peerConn.sendGateWayMsg(uid, msgId.getNumber(), message);
    }

// 获取场景玩家信息
    public ProtoScene.ScenePlayerInfo getScenePlayerInfo()
    {
        ProtoScene.ScenePlayerInfo.Builder info = ProtoScene.ScenePlayerInfo.newBuilder();
        info.setUid(uid);
        info.setPeerId(getSceneModule().getPeerId());
        info.setName(getBasicModule().getNickName());
        info.setSceneId(getSceneModule().getCurSceneId());
//        getMpComp().fillOnlinePlayerInfo(*info.mutable_online_player_info());
        return info.build();
    }

    public boolean isConnected() {
        return true;
    }
}
