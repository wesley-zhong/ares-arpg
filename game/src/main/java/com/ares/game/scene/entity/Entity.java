package com.ares.game.scene.entity;

import com.ares.common.math.Coordinate;
import com.ares.common.math.Transform;
import com.ares.common.math.Vector3;
import com.ares.core.exception.UnknownLogicException;
import com.ares.core.utils.TimeUtils;
import com.ares.game.player.Player;
import com.ares.game.scene.Grid;
import com.ares.game.scene.Scene;
import com.ares.game.scene.VisionContext;
import com.ares.game.scene.visitor.Visitor;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoMsgId;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

import static com.game.protoGen.ProtoScene.ProtEntityType.*;

// 场景上的实体
@Getter
@Setter
@Slf4j
public abstract class Entity {
    // 移动上下文
    @Getter
    @Setter
    public static class MotionContext {
        // 输入字段
        long sceneTimeMs = 0;
        long excludeUid = 0;
        boolean notify = false;
        // 输出字段
        Collection<Long> syncUidList;
        boolean doMove = true;
    }

    ;

    private int entityId;
    private int configId;
    private boolean clientCreated = false;
    private boolean clientVisible = true;
    private long onlyVisionUid = 0;  // 非0时，在场景上只有UID的客户端才可见
    private long deadClearTime = 0;  // 死亡清除时间
    private String entityName;
    private Scene scene;
    private Grid grid;
    private ProtoCommon.VisionLevelType visionLevelType = ProtoCommon.VisionLevelType.VISION_LEVEL_NORMAL;
    private int roomId = 0;  // 在房间场景需要确定玩家的room_id
    private Coordinate coordinate;      // 以grid为单位的坐标
    private Vector3 position;       // 实际坐标
    private Vector3 rotation;      // 旋转(角度，欧拉角，左手坐标系)
    private Vector3 speed = Vector3.ZERO;         // 当前的速度
    private float maxSpeedLength = 0;    // 最大速度
    private ProtoScene.MotionState motionState = ProtoScene.MotionState.MOTION_NONE;      // 当前的移动状态
    private long lastMotionStateChangeTimeMs = 0;    // 上一次移动状态改变时间
    private long lastMoveSceneTimeMs = 0;
    //    SceneEntityAiInfo ai_info_;                 // AI数据
    private Vector3 bornPos;                          // 出生位置
    private Vector3 bornRot;                          // 出生位置
    private final EntityViewMgr viewMgr = new EntityViewMgr();
    private long enterSceneTime = 0;

    public abstract ProtoScene.ProtEntityType getEntityType();

    // Visitor设计模式，实现accept方法
    public int accept(Visitor visitor) {
        return visitor.visitEntity(this);
    }

    // 获取所属player(该实体是player的一部分)
    public Player getPlayer() {
        return null;
    }

    public long getPlayerUid() {
        Player player = getPlayer();
        if (player != null) {
            return player.getUid();
        }
        return 0;
    }

    // 获取所属player(该实体不是player的一部分)
    public Player getOwnerPlayer() {
        return null;
    }

    // 设置出生点位置
    public void setBornPosition(Vector3 pos) {
        setPosition(pos);
        bornPos = pos;
    }

    // 检查位置
    public int checkPosition(Vector3 pos) {
        return 0;
    }

    // 位置和朝向
    public Transform getTransform() {
        return new Transform(position, rotation);
    }

    // 是否可以进入区域
    public boolean canEnterRegion() {
        return false;
    }

    // 是否有任意玩家在查看
    public boolean isAnyPlayerViewing() {
        return !viewMgr.getViewingPlayerMap().isEmpty();
    }

    public EntitySightGroup getSightGroup() {
        return viewMgr.getSightGroup();
    }

    public void setSightGroup(EntitySightGroup sightGroup) {
        viewMgr.setSightGroup(sightGroup);
    }

    public EntitySightGroup getOrCreateSightGroup() {
        return null;
//
//        if (viewMgr.sight_group_ == null)
//        {
//            viewMgr.sight_group_ = MAKE_SHARED<EntitySightGroup>();
//            if (viewMgr.sight_group_ == null)
//            {
//                LOG_ERROR("viewMgr.sight_group_ construct failed");
//                return null;
//            }
//            CreaturePtr creature = toPtr<Creature>(this);
//            if (creature != null)
//            {
//                viewMgr.sight_group_.setAuthorityPeerId(creature.getAuthorityPeerId());
//            }
//            viewMgr.sight_group_.addEntityInSightGroup(shared_from_this());
//            for (auto ele : viewMgr.validateAndGetViewingPlayers())
//            {
//                viewMgr.sight_group_.onPlayerDoView(ele);
//            }
//            viewMgr.sight_group_.setGroupVisionType(getGroupVisionType());
//        }
//
//        return viewMgr.sight_group_;
    }

    public EntitySightGroup getPreInstallSightGroup() {
        return viewMgr.getPreinstallSightGroup();
    }

    public void setPreInstallSightGroup(EntitySightGroup sightGroup) {
        viewMgr.setPreinstallSightGroup(sightGroup);
    }

    public ProtoScene.GroupVisionType getGroupVisionType() {
        return viewMgr.getGroupVisionType();
    }

    public void setGroupVisionType(ProtoScene.GroupVisionType type) {
        // entity会在创建或初始化时设置group_vision_type_，之后不会改变
        // 这里简化逻辑处理,保证设置sight_group_也不会变
        if (viewMgr.getSightGroup() != null && viewMgr.getSightGroup().getGroupVisionType() != type) {
            throw new UnknownLogicException("entity:" + this + ". sight_group is not null and vision_type:"
                    + viewMgr.getSightGroup().getGroupVisionType() + " != " + type);
        }
        viewMgr.setGroupVisionType(type);
    }

    // 是否在场景上
    public boolean isOnScene() {
        return getScene() != null && grid != null;
    }

    public void enterScene(Scene scene, VisionContext context) {
        setScene(scene);
        onBeforeEnterScene(scene, context);
        scene.entityAppear(this, context);
        enterSceneTime = TimeUtils.currentTimeMillis();
        onAfterEnterScene(scene, context);
    }

    public void onBeforeEnterScene(Scene scene, VisionContext context) {

    }

    public void onAfterEnterScene(Scene scene, VisionContext context) {

    }

    public void leaveScene(VisionContext context, long deadClearTime) {
        onBeforeLeaveScene(context);
        if (scene != null) {
            scene.entityDisappear(this, context, deadClearTime);
        } else {
            log.debug("scene is null, config_id: " + getConfigId());
        }
        onAfterLeaveScene(context);
    }

    public void onBeforeLeaveScene(VisionContext context) {

    }

    public void onAfterLeaveScene(VisionContext context) {

    }

    ;

    // 进入角色的视野时的回调
    public void onEnterPlayerView(Player player) {
        viewMgr.onPlayerDoView(player);
//        GroupPtr group = getGroup();
//        if (group != null)
//        {
//            group.onEnterPlayerView(player, *this);
//        }
    }

    // 离开角色的视野时的回调
    public void onExitPlayerView(Player player) {
        viewMgr.onPlayerUndoView(player);
//        GroupPtr group = getGroup();
//        if (group != null)
//        {
//            group.onExitPlayerView(player, *this);
//        }
    }

    // 转换为协议的类型
    private static ProtoScene.ProtEntityType toClient(ProtoScene.ProtEntityType entity_type) {
        switch (entity_type) {
            case PROT_ENTITY_AVATAR:
                return PROT_ENTITY_AVATAR;
            case PROT_ENTITY_MONSTER:
                return PROT_ENTITY_MONSTER;
            case PROT_ENTITY_NPC:
                return PROT_ENTITY_NPC;
            case PROT_ENTITY_GADGET:
            case PROT_ENTITY_WEAPON:
            case PROT_ENTITY_WEATHER:
                return PROT_ENTITY_GADGET;
            default:
                log.error("unknown entity type: " + entity_type);
                return PROT_ENTITY_NONE;
        }
    }

    public void toClient(ProtoScene.SceneEntityInfo.Builder pb) {
        ProtoScene.ProtEntityType entity_type = getEntityType();
        pb.setEntityType(toClient(entity_type));
        pb.setEntityId(entityId);
        if (entityName != null) {
            pb.setName(entityName);
        }
        getLastMotionInfo(pb.getMotionInfoBuilder());
    }

    // 获取当前的移动信息
    public void getLastMotionInfo(ProtoScene.MotionInfo.Builder motionInfo) {
        position.toClient(motionInfo.getPosBuilder());
        rotation.toClient(motionInfo.getRotBuilder());
        speed.toClient(motionInfo.getSpeedBuilder());
        motionInfo.setState(motionState);
        motionInfo.setSceneTime(lastMoveSceneTimeMs);
    }

    // 设置当前的移动信息
    public void setMotionInfo(ProtoScene.MotionInfo motionInfo, MotionContext motionContext) {
        if (scene == null) {
            throw new UnknownLogicException("entity is not on scene. " + this);
        }
        if (setMotionState(motionInfo, motionContext.sceneTimeMs) < 0) {
            throw new UnknownLogicException("setMotionState fails. " + this);
        }

        boolean broadcast = true;
        Vector3 pos = Vector3.fromClient(motionInfo.getPos());

        switch (motionInfo.getState()) {
            case MOTION_NONE:
                return;
            case MOTION_FIGHT:
            case MOTION_LAND_SPEED:
                // 这两个状态都不包含pos
                motionContext.doMove = false;
                broadcast = false;
                break;
            case MOTION_NOTIFY:
                // 这个状态可能很久才同步一次
                broadcast = false;
                break;
            case MOTION_FALL_ON_GROUND:
                // TODO...宁烨说客户端正在迭代位置同步相关代码，预计一两个月时间，这里做紧急处理 2019-08-07
//                if (proto::PROT_ENTITY_AVATAR == getEntityType() && pos.x < FLT_EPSILON && pos.z < FLT_EPSILON)
//                {
//                    pos.x = getPosition().x;
//                    pos.z = getPosition().z;
//                }
                break;
            default:
                break;
        }
        Vector3 speed = Vector3.fromClient(motionInfo.getSpeed());
        if (motionContext.doMove) {
            this.rotation = Vector3.fromClient(motionInfo.getRot());
            this.speed = speed;
            if (0 != checkMoveSpeed(motionInfo.getState(), pos, motionContext)) {
                throw new UnknownLogicException("checkMoveSpeed fails, target:" + pos + " cur_pos:" + position + " entity:" + this);
            }
            scene.entityMoveTo(this, pos);
        }
        if (broadcast) {
            if (motionContext.notify) {
                broadcastMotionInfo(motionInfo, motionContext);
            }
            motionContext.syncUidList = scene.getViewingPlayers(this, motionContext.excludeUid, true);
        }
        lastMoveSceneTimeMs = motionContext.sceneTimeMs;
    }

    // 强制设置实体的运动状态，任务使用，不同步
    public void forceSetMotionInfo(ProtoScene.MotionInfo motionInfo, long sceneTimeMs) {
        if (scene == null) {
            throw new UnknownLogicException("entity is not on scene. " + this);
        }
        if (setMotionState(motionInfo, sceneTimeMs) < 0) {
            throw new UnknownLogicException("setMotionState fails. " + this);
        }
        // TODO: 先暂时把rotation_放到前面，要考虑如何保证原子性
        Vector3 pos = Vector3.fromClient(motionInfo.getPos());
        rotation = Vector3.fromClient(motionInfo.getRot());
        speed = Vector3.fromClient(motionInfo.getSpeed());
        lastMoveSceneTimeMs = sceneTimeMs;
        scene.entityMoveTo(this, pos);
    }

    // 设置当前的移动状态
    int setMotionState(ProtoScene.MotionInfo motionInfo, long sceneTimeMs) {
        ProtoScene.MotionState motionState = motionInfo.getState();
        // 只更新位置，不影响逻辑
        if (motionState == ProtoScene.MotionState.MOTION_FORCE_SET_POS) {
            return 1;
        }
        switch (motionState) {
            case MOTION_NONE:
            case MOTION_NOTIFY:
            case MOTION_LAND_SPEED:
                return 0;
            default:
                break;
        }
        if (this.motionState == motionState)
            return 1;
        //LOG_DEBUG + "[MOTION] change from state:" + proto::MotionState_Name(motion_state_) + ", to state:" + proto::MotionState_Name(motionState) + *this;
        // 如果上一个状态是FIGHT，后面的状态不是FIGHT需要发送reliable包
        if (this.motionState == ProtoScene.MotionState.MOTION_FIGHT) {
//            is_move_sent_reliable_ = true;
        }
        this.motionState = motionState;
        lastMotionStateChangeTimeMs = sceneTimeMs;
        return 0;
    }

    // 超速检查
    public int checkMoveSpeed(ProtoScene.MotionState state, Vector3 pos, MotionContext motionContext) {
        return 0;
    }

    // 广播移动信息
    void broadcastMotionInfo(ProtoScene.MotionInfo motionInfo, MotionContext motionContext) {
        if (scene == null) {
            throw new UnknownLogicException("entity is not on scene. " + this);
        }

        // ZT_TODO 合包
        ProtoScene.SceneEntitiesMoveNtf.Builder notify = ProtoScene.SceneEntitiesMoveNtf.newBuilder();
        ProtoScene.EntityMoveInfo.Builder moveInfo = ProtoScene.EntityMoveInfo.newBuilder();
        moveInfo.setEntityId(entityId);
        moveInfo.setMotionInfo(motionInfo);
        notify.addEntityMoveInfoList(moveInfo);
        scene.notifyViewingPlayersExclude(this, ProtoMsgId.MsgId.SCENE_ENTITIES_MOVE_NTF, notify.build(), motionContext.excludeUid, true);
    }

    // 获取上一次的移动通知协议
    ProtoScene.EntityMoveInfo getLastMoveNotify() {
        ProtoScene.EntityMoveInfo.Builder builder = ProtoScene.EntityMoveInfo.newBuilder();
        builder.setEntityId(entityId);
        getLastMotionInfo(builder.getMotionInfoBuilder());
        return builder.build();
    }

    // 通知上一次的移动信息
    public void notifyLastMotionInfo(Player player) {
        if (motionState == ProtoScene.MotionState.MOTION_NONE)
            return;
        Player ownerPlayer = getPlayer();
        if (ownerPlayer == player)
            return;
        ProtoScene.SceneEntitiesMoveNtf.Builder notify = ProtoScene.SceneEntitiesMoveNtf.newBuilder();
        notify.addEntityMoveInfoList(getLastMoveNotify());
        player.sendMessage(ProtoMsgId.MsgId.SCENE_ENTITIES_MOVE_NTF, notify.build());
    }

    public void notifyLastMotionInfo(Collection<Long> uids) {
        if (motionState == ProtoScene.MotionState.MOTION_NONE)
            return;
        ProtoScene.SceneEntitiesMoveNtf.Builder notify = ProtoScene.SceneEntitiesMoveNtf.newBuilder();
        notify.addEntityMoveInfoList(getLastMoveNotify());
//        return GAME_THREAD_LOCAL.player_mgr.sendToPlayerList(uid_vec, CONST_MESSAGE_PTR(notify));
    }

    public int getSceneId() {
        if (scene != null) {
            return scene.getSceneId();
        }
        return 0;
    }
}
