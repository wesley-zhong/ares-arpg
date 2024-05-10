package com.ares.game.player.modules.scene;

import com.ares.common.math.Transform;
import com.ares.common.math.Vector3;
import com.ares.core.excetion.FyLogicException;
import com.ares.core.excetion.UnknownLogicException;
import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneUtil;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.subclass.PlayerWorldScene;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.ares.game.service.PlayerRoleService;
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
    }

    public enum BackMyWorldReason
    {
        E_BACK_MY_WORLD_BY_PLAYER_REQ,
        E_BACK_MY_WORLD_BY_HOST_BE_BLOCK,
    }

    // 需要持久化的变量
    private int myCurSceneId = 0;          // 在自己世界时的scene_id,用于db.login的恢复
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
        myPlayerWorld.init();
    }

    @Override
    public void start() {
        myPlayerWorld.start();
    }

    public void joinPlayerScene(long targetUid) {
        if (targetUid == getPlayer().getUid())
        {
            throw new UnknownLogicException("joinPlayerScene targetUid cannot be self, uid:" + getPlayer().getUid());
        }

        Player target_player = PlayerRoleService.Instance.getPlayer(targetUid);
        if (target_player == null)
        {
            throw new FyLogicException(ProtoCommon.ErrCode.RET_PLAYER_NOT_ONLINE_VALUE, "findOnlinePlayer failed, targetUid:" + targetUid + " uid:" + getPlayer().getUid());
        }
        PlayerWorld target_world = target_player.getSceneModule().getMyPlayerWorld();
        if (target_world == null)
        {
            throw new UnknownLogicException("target_player getMyPlayerWorld fails, target: " + targetUid + " uid: " + getPlayer().getUid());
        }

        Scene cur_scene = getCurScene();
        Scene dest_scene = target_player.getSceneModule().getDestScene();
        if (dest_scene == null)
        {
            dest_scene = target_player.getSceneModule().getCurScene();
        }
        if (dest_scene == null)
        {
            throw new UnknownLogicException("dest_scene is null, targetUid:" + targetUid + " uid:" + getPlayer().getUid());
        }
        if (dest_scene.getSceneType() == ProtoCommon.SceneType.SCENE_DUNGEON)
        {
            dest_scene = target_player.getSceneModule().getMainWorldScene();
            if (dest_scene == null)
            {
                throw new UnknownLogicException("getMainWorldScene fails, targetUid: " + targetUid + " uid: " + getPlayer().getUid());
            }
        }
        if (dest_scene.getOwnerUid() != targetUid)
        {
            throw new UnknownLogicException("dest_scene.getOwnerUid() != targetUid: " + targetUid + " uid: " + getPlayer().getUid());
        }
//        if (!getPlayer().getBasicModule().isStateOpen(OPEN_STATE_MULTIPLAYER))
//        {
//            LOG_ERROR + "player mp switch not open, uid:" + getPlayer().getUid();
//            return proto::RET_MP_OPEN_STATE_FAIL;
//        }
//        if (getPlayer().getBasicModule().getWorldLevel() < target_player.getBasicModule().getWorldLevel())
//        {
//            LOG_WARNING + "player world level lower than target player, uid:" + getPlayer().getUid();
//            return proto::RET_FAIL;
//        }
//        auto [retcode, _] = getPlayer().checkMpModeAvailability();
//        if (0 != retcode)
//        {
//            LOG_WARNING + "mp is not enterable, " + " retcode:" + retcode + " uid:" + getPlayer().getUid();
//            return retcode;
//        }
//        if (!getPlayer().getMpModule().isAllowEnterPlayerEmpty())
//        {
//            LOG_WARNING + "others is entering self world, uid: " + getPlayer().getUid();
//            return proto::RET_MP_OTHER_ENTERING;
//        }
        // 通用匹配策划要求单人联机可以作为客机进入别人世界
//        PlayerWorld my_world = getPlayer().getSceneModule().getMyPlayerWorld();
//        if (null != my_world && my_world.getPlayerCount() > 1)
//        {
//            LOG_WARNING + "uid is already in mp mode, uid: " + getPlayer().getUid();
//            return proto::RET_MP_IN_MP_MODE;
//        }
//        if (getPlayer().getMpModule().isInMpMode() && !getPlayer().getMatchModule().isAfterGeneralMatch())
//        {
//            LOG_WARNING + "uid is already in mp mode, uid: " + getPlayer().getUid();
//            return proto::RET_MP_IN_MP_MODE;
//        }
//
//        retcode = target_player.getMpModule().checkMpSceneEnterable(getPlayer().getUid());
//        if (0 != retcode)
//        {
//            LOG_WARNING + "target_player dest_scene is not enterable, targetUid:" + targetUid + " retcode:" + retcode + " uid:" + getPlayer().getUid();
//            return retcode;
//        }
//
//        retcode = target_player.getMpModule().checkAllowEnterByPlayerUid(getPlayer().getUid());
//        if (0 != retcode)
//        {
//            LOG_WARNING + "target_player not allow enter, targetUid:" + targetUid + " retcode:" + retcode + " uid:" + getPlayer().getUid();
//            return retcode;
//        }

//        const SceneScriptConfig* scene_script_config = dest_scene.getScriptConfig();
//        if (scene_script_config == null)
//        {
//            LOG_WARNING + "getScriptConfig fails" + *dest_scene;
//            return -1;
//        }
        Vector3 born_pos = Vector3.ZERO;
        Vector3 born_rot = Vector3.ZERO;

        Scene target_dest_scene = target_player.getSceneModule().getDestScene();
        Scene target_cur_scene = target_player.getSceneModule().getCurScene();
        // 如果有场景多阶段玩法强拉的需求
//        if (target_cur_scene != null && target_cur_scene.getMultistagePlayModule().canSetGuestUidPosAndRot(getPlayer().getUid())
//                && 0 == target_cur_scene.getMultistagePlayModule().setGuestUidPosAndRot(getPlayer().getUid(), born_pos, born_rot))
//        {
//            LOG_DEBUG + "MultistagePlayModule set target pos:" + born_pos + ", rot:"+ born_rot + ", uid:" + getPlayer().getUid();
//        }
//        else if (target_dest_scene != null && target_dest_scene.getSceneId() == dest_scene.getSceneId())
//        {
//            // 在传送中，取目标玩家传送终点
//            born_pos = target_player.getSceneModule().getDestPos();
//            born_rot = target_player.getSceneModule().getDestRot();
//            LOG_DEBUG + "target isInTransfer, choose dest pos and rot, uid:" + getPlayer().getUid();
//        }
//        else
//        {
            Scene.ScenePlayerLocation playerLocation = dest_scene.getPlayerLocation(target_player.getUid());
            if (playerLocation != null)
            {
                born_pos = playerLocation.lastValidPos;
                born_rot = playerLocation.lastValidRot;
            }
//        }

        // 如果不在联机中，切入联机大世界
//        if (!target_player.getMpModule().isInMpMode())
//        {
//            target_player.getSceneModule().changeMyWorldToMpMode();
//        }
//
//        if (data::SceneType::SCENE_DUNGEON == dest_scene.getSceneType())
//        {
//            getPlayer().getDungeonModule().setIsJoinOtherScene(true);
//        }
        recordMySceneInfo();
        jumpToScene(dest_scene, born_pos, born_rot, 0, ProtoScene.EnterReason.ENTER_REASON_TEAM_JOIN);
    }


    public void backMyWorld(BackMyWorldReason reason)
    {
        // 踢到原来的位置
        PrevSceneInfo prevSceneInfo = getMyPrevSceneInfo();
        Scene destScene = getPersonalScene(prevSceneInfo.prevSceneId);
        if (destScene == null)
        {
            throw new UnknownLogicException("get prevScene failed. uid:" + getPlayer().getUid() + " sceneId:" + prevSceneInfo.prevSceneId);
        }

//        World cur_world = getCurWorld();
//        auto notify = MAKE_SHARED<proto::PlayerQuitFromMpNotify>();
//        proto::PlayerQuitFromMpNotify_QuitReason proto_reason;
//        switch(reason)
//        {
//            case E_BACK_MY_WORLD_BY_PLAYER_REQ:
//            {
//                proto_reason = proto::PlayerQuitFromMpNotify::BACK_TO_MY_WORLD;
//            }
//            break;
//            case E_BACK_MY_WORLD_BY_HOST_BE_BLOCK:
//            {
//                proto_reason = proto::PlayerQuitFromMpNotify::BE_BLOCKED;
//            }
//            break;
//        }
//        notify.set_reason(proto_reason);
//        player_.sendMessage(CONST_MESSAGE_PTR(notify));

        jumpToScene(destScene, prevSceneInfo.pos, prevSceneInfo.rot, 0, ProtoScene.EnterReason.ENTER_REASON_TEAM_BACK);
    }

    // 记录之前自己的场景信息
    private void recordMySceneInfo()
    {
        Avatar cur_avatar = getPlayer().getAvatarModule().getCurAvatar();
        if (cur_avatar == null)
        {
            log.warn("cur_avatar is null" + getPlayer());
            return;
        }
        Scene cur_scene = getCurScene();
        if (cur_scene == null)
        {
            log.debug("cur_scene is null" + getPlayer());
            return;
        }
        if (!SceneUtil.isPlayerScene(cur_scene.getSceneType()))
        {
            // 非玩家scene，不记录
            return;
        }
        myPrevSceneId = cur_avatar.getSceneId();
    }

    static class PrevSceneInfo{
        int prevSceneId;
        Vector3 pos;
        Vector3 rot;
    }

    // 获取之前自己的场景ID
    private PrevSceneInfo getMyPrevSceneInfo(){
        PrevSceneInfo result = new PrevSceneInfo();
        if (myPrevSceneId != 0) {
            result.prevSceneId = myPrevSceneId;
        }
        else {
            result.prevSceneId = PlayerWorld.getDefaultWorldMainSceneId();
        }

        Scene prev_scene = findScene(result.prevSceneId);
        if (prev_scene == null)
        {
            throw new UnknownLogicException("findScene failed, uid:" + getPlayer().getUid() + " scene_id:" + result.prevSceneId);
        }

        Transform transform = prev_scene.getPlayerValidLocation(player.getUid());
        result.pos = transform.getPosition();
        result.rot = transform.getRotation();

        return result;
    }

    // 跳到目的场景
    private void jumpToScene(Scene scene, Vector3 pos, Vector3 rot, int point_type, ProtoScene.EnterReason reason)
    {
        Avatar cur_avatar = getPlayer().getCurAvatar();
        if (cur_avatar == null)
        {
            throw new UnknownLogicException("getCurAvatar fails" + getPlayer());
        }
        ProtoScene.EnterType enter_scene_type = ProtoScene.EnterType.ENTER_GOTO;
        ProtoScene.VisionType vision_type = ProtoScene.VisionType.VISION_MEET;
        Scene cur_scene = getCurScene();
        if (cur_scene != scene)
        {
            ProtoCommon.SceneType dest_scene_type = scene.getSceneType();
            if (dest_scene_type == ProtoCommon.SceneType.SCENE_DUNGEON)
            {
                enter_scene_type = ProtoScene.EnterType.ENTER_DUNGEON;
            }
            else
            {
                Player owner_player = scene.getOwnPlayer();
                if (owner_player != null)
                {
                    Player player = getPlayer();
                    if (owner_player != player)
                    {
                        enter_scene_type = ProtoScene.EnterType.ENTER_OTHER;
                    }
                    else if (curWorldOwnerUid > 0 && curWorldOwnerUid != getPlayer().getUid())
                    {
                        // 从别人的大世界回到自己的大世界(可能主机已经被销毁)
                        enter_scene_type = ProtoScene.EnterType.ENTER_BACK;
                    }
                    else
                    {
                        enter_scene_type = ProtoScene.EnterType.ENTER_JUMP;
                    }
                }
            }
        }
        else
        {
//            if (static_cast<uint32_t>(ScenePointType::PORTAL) == point_type)
//            {
//                enter_scene_type = proto::ENTER_GOTO_BY_PORTAL;
//            }
            // 在同一个场景内进行传送
            vision_type = ProtoScene.VisionType.VISION_TRANSPORT;
            // 未完成进场或上次非传送的进程流程
            // 实际上只会在SceneInitFinish(设置了cur scene)与EnterSceneDone(清理dest)之间走到这个分支
            if (getDestScene() != null  && getDestEnterType() != ProtoScene.EnterType.ENTER_GOTO)
            {
                log.debug("cannot goto while in enter scene process, player:" + getPlayer() + " goto_scene:" + scene);
//#ifdef HK4E_DEBUG
//                auto notify = MAKE_SHARED<proto::ServerLogNotify>();
//                notify.set_log_level(proto::LOG_LEVEL_INFO);
//                string server_log = "无法在进程流程尚未结束时进行传送, scene:" + scene.getDesc();
//                notify.set_server_log(server_log);
//                getPlayer().sendMessage(CONST_MESSAGE_PTR(notify));
//#endif
                throw new UnknownLogicException("cannot goto while in enter scene process, player:" + getPlayer() + " goto_scene:" + scene);
            }
        }
        getPlayer().beginEnterScene(scene, new Transform(pos, rot), enter_scene_type, vision_type, false, false, reason);
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
            curScene.erasePlayerViewMgr(getPlayer().getUid());
//            destroyEntityWhenLeaveScene();
        }

        curScene = targetScene;
        if (curScene != null)
        {
            curScene.insertPlayerViewMgr(getPlayer());
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

    // 查找玩家的个人场景
    Scene findScene(int sceneId)
    {
        if (myPlayerWorld == null)
        {
            throw new UnknownLogicException("myPlayerWorld is null");
        }
        return myPlayerWorld.findScene(sceneId);
    }

    // 获得玩家的个人场景(如果没有，则创建)
    Scene getPersonalScene(int sceneId)
    {
        if (myPlayerWorld == null)
        {
            throw new UnknownLogicException("myPlayerWorld is null");
        }
        return myPlayerWorld.getScene(sceneId);
    }

    public PlayerWorldScene getMainWorldScene(){
        if (myPlayerWorld == null)
        {
            throw new UnknownLogicException("myPlayerWorld is null");
        }
        return myPlayerWorld.getMainWorldScene();
    }
}
