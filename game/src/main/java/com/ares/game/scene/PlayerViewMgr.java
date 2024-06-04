package com.ares.game.scene;

import com.ares.core.exception.UnknownLogicException;
import com.ares.game.player.Player;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.entity.EntityMgr;
import com.ares.game.scene.entity.EntitySightGroup;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.entity.eyepoint.PlayerEyePoint;
import com.game.protoGen.ProtoMsgId;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Getter
@Setter
@Slf4j
public class PlayerViewMgr {
    enum PlayerChangeEyePropGuardState {
        PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE,
        PLAYER_CHANGE_EYE_PROP_GUARD_STATE_GUARDING,
        PLAYER_CHANGE_EYE_PROP_GUARD_STATE_DONE
    }

    ;

    static class PlayerChangeEyePropGuard implements AutoCloseable {
        private final boolean work;
        private PlayerViewMgr playerViewMgr;
        private final Map<Integer, Entity> oldViewEntityMap = new HashMap<>();
        private final Map<Integer, EntitySightGroup> oldViewSightGroupMap = new HashMap<>();

        public PlayerChangeEyePropGuard(PlayerViewMgr playerViewMgr, boolean work) {
            this.work = work;

            if (!this.work) {
                return;
            }
            if (playerViewMgr == null) {
                throw new UnknownLogicException("playerViewMgr is null");
            }
            if (playerViewMgr.getChangeEyePropGuardState() != PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE) {
                return;
            }

            this.playerViewMgr = playerViewMgr;
            playerViewMgr.setChangeEyePropGuardState(PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_GUARDING);

            oldViewEntityMap.putAll(playerViewMgr.viewEntityMap);
            oldViewSightGroupMap.putAll(playerViewMgr.viewSightGroupMap);
        }

        @Override
        public void close() {
            if (!work) {
                return;
            }
            if (null == playerViewMgr) {
                return;
            }
            if (playerViewMgr.getChangeEyePropGuardState() != PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_GUARDING) {
                throw new UnknownLogicException("state:" + playerViewMgr.getChangeEyePropGuardState() + " not guarding" + " uid:" + playerViewMgr.getPlayerUid());
            }
            Scene scene = playerViewMgr.getScene();
            if (scene == null) {
                throw new UnknownLogicException("scene is null. uid:" + playerViewMgr.getPlayerUid());
            }
            Player player = playerViewMgr.getPlayer();
            if (null == player) {
                throw new UnknownLogicException("player is null. uid:" + playerViewMgr.getPlayerUid());
            }

            playerViewMgr.setChangeEyePropGuardState(PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_DONE);

            try {
                List<Entity> appearEntityList = new ArrayList<>();
                List<Entity> disappearEntityList = new ArrayList<>();
                Map<Integer, Entity> curViewEntityMap = playerViewMgr.viewEntityMap;
                for (Entity e : curViewEntityMap.values()) {
                    int entityId = e.getEntityId();
                    if (oldViewEntityMap.remove(entityId) == null) {
                        appearEntityList.add(e);
                    }
                }
                disappearEntityList.addAll(oldViewEntityMap.values());

                List<EntitySightGroup> appearSightGroups = new ArrayList<>();
                List<EntitySightGroup> disappearSightGroups = new ArrayList<>();
                Map<Integer, EntitySightGroup> curViewSightGroupMap = playerViewMgr.viewSightGroupMap;
                for (EntitySightGroup sg : curViewSightGroupMap.values()) {
                    int sightGroupId = sg.getSelfId();
                    if (oldViewSightGroupMap.remove(sightGroupId) == null) {
                        appearSightGroups.add(sg);
                    }
                }
                disappearSightGroups.addAll(oldViewSightGroupMap.values());

//#ifdef HK4E_DEBUG
                log.debug("uid:{} disappear_entity_count:{} appear_entity_count:{} disappear_sight_group_count:{} appear_sight_group_count:{}"
                        , playerViewMgr.getPlayerUid(), disappearEntityList.size(), appearEntityList.size()
                        , disappearSightGroups.size(), appearSightGroups.size());
//#endif

                for (EntitySightGroup sight_group : disappearSightGroups) {
                    sight_group.onExitPlayerView(player);
                }
                for (EntitySightGroup sight_group : appearSightGroups) {
                    sight_group.onEnterPlayerView(player);
                }

                scene.notifyEntityDisappear(player, disappearEntityList, VisionContext.MISS);
                scene.notifyEntityAppear(player, appearEntityList, VisionContext.MEET);
            } finally {
                playerViewMgr.setChangeEyePropGuardState(PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE);
            }
        }
    }

    ;

    private final Scene scene;    // 所属的scene
    private final Player player;  // 所属的玩家
    private boolean eyePointAsEye = false;  // eye_point是否用作眼睛
    private PlayerEyePoint eyePoint;   // 观察点
    private final Map<Integer, Entity> viewEntityMap = new HashMap<>();     // 正在查看的entity集合
    private final Map<Integer, Entity> inSightRangeEntityMap = new HashMap<>();  // 视野范围内entity集合(空间地理上的概念)
    private final Map<Integer, EntitySightGroup> viewSightGroupMap = new HashMap<>();     // 正在查看的sight_group集合
    private PlayerChangeEyePropGuardState changeEyePropGuardState = PlayerChangeEyePropGuardState.PLAYER_CHANGE_EYE_PROP_GUARD_STATE_NONE;
    private Set<Integer> groupVisionTypeSet = new HashSet<>();

    public PlayerViewMgr(final Scene scene, final Player player) {
        this.scene = scene;
        this.player = player;

        doResetGroupVisionTypeSet();
    }

    public long getPlayerUid() {
        return player.getUid();
    }

    public void resetPlayerViewMgr() {
        if (eyePoint != null) {
            eyePoint.leaveScene(VisionContext.MISS, 0);
            log.debug("clearEyePoint. uid:{} eye_point:{}", getPlayerUid(), eyePoint);
            eyePointAsEye = false;
            eyePoint = null;
            notifyEyePointState();
        }
        doResetGroupVisionTypeSet();
        clearViewContainer();
    }

    public void clearViewContainer() {
        viewEntityMap.clear();
        inSightRangeEntityMap.clear();
        viewSightGroupMap.clear();
    }

    public void setEyePoint(Region region, Region relatedBigRegion) {
        Player player = getPlayer();
        if (player == null) {
            throw new UnknownLogicException("getPlayer() failed." + " uid:" + getPlayerUid());
        }
        Scene scene = region.getScene();
        if (scene == null) {
            throw new UnknownLogicException("region.getScene() failed." + " uid:" + getPlayerUid());
        }

        int curEyePointRegionEntityId = 0;
        if (eyePoint != null) {
            curEyePointRegionEntityId = eyePoint.getRegionEntityId();
            // 相同区域，不需要重复设置
            if (curEyePointRegionEntityId == region.getEntityId()) {
                return;
            }
        }

        boolean eyePointAsEyeClosed = SceneUtil.isEyePointAsEyeClosed();
        try (PlayerChangeEyePropGuard guard = new PlayerChangeEyePropGuard(this, !eyePointAsEyeClosed)) {

            if (curEyePointRegionEntityId != 0) {
                clearEyePoint(curEyePointRegionEntityId, true);
            }

            eyePoint = EntityMgr.createPlayerEyePoint(player, scene, region, relatedBigRegion);
            eyePointAsEye = !eyePointAsEyeClosed;

            log.debug("uid:{} setEyePoint. eyePoint entityId:{} region:{}", getPlayerUid(), eyePoint.getEntityId(), region);
            notifyEyePointState();

            if (!eyePointAsEyeClosed) {
//                scene.getBlockGroupComp().onPlayerEyePosChange(getPlayerUid(), eyePoint.getPosition());
            }

            // 协程resume之后 eyePoint可能为空
            if (eyePoint != null) {
                eyePoint.enterScene(scene, VisionContext.MEET);
            }
        }
    }

    public void clearEyePoint(int targetRegionEntityId, boolean changeToAnotherEyePoint) {
        // 外圈比内圈大，没进内圈，离开外圈的话eyePoint就是null
        if (eyePoint == null) {
//#ifdef HK4E_DEBUG
            log.debug("eyePoint is null. uid:" + getPlayerUid());
//#endif
            return;
        }

        // 不相等是正常情况，因为离开的区域可能不是当前eye_point对应的区域
        if (eyePoint.getRegionEntityId() != targetRegionEntityId) {
//#ifdef HK4E_DEBUG
            log.debug("uid:" + getPlayerUid() + " cur_eye_point region_entity_id:" + eyePoint.getRegionEntityId()
                    + " != " + "targetRegionEntityId:" + targetRegionEntityId);
//#endif
            return;
        }

        boolean eyePointAsEyeClosed = SceneUtil.isEyePointAsEyeClosed();

        try (PlayerChangeEyePropGuard guard = new PlayerChangeEyePropGuard(this, !eyePointAsEyeClosed)) {

            eyePoint.leaveScene(VisionContext.MISS, 0);

            log.debug("clearEyePoint. uid:{} eye_point:{} changeToAnotherEyePoint:{}", getPlayerUid(), eyePoint, changeToAnotherEyePoint);
            eyePointAsEye = false;
            eyePoint = null;

            if (!changeToAnotherEyePoint) {
                notifyEyePointState();
                if (!eyePointAsEyeClosed) {
                    Player player = getPlayer();
                    if (player == null) {
                        throw new UnknownLogicException("player is null" + " uid:" + getPlayerUid());
                    }

                    Avatar avatar = player.getCurAvatar();
                    if (avatar == null) {
                        throw new UnknownLogicException("avatar is null" + " uid:" + getPlayerUid());
                    }
                    if (!avatar.isOnScene()) {
                        return;
                    }

                    Scene scene = avatar.getScene();
                    if (scene == null) {
                        throw new UnknownLogicException("scene is null" + " uid:" + getPlayerUid());
                    }

//                    scene.getBlockGroupComp().onPlayerEyePosChange(getPlayerUid(), avatar.getPosition());
                    scene.refreshPlayerInSightRangeEntitiesAndEnter(player);
                }
            }
        }
    }

    public PlayerEyePoint getEyePointAsEye() {
        if (!eyePointAsEye) {
            return null;
        }
        return eyePoint;
    }

    public boolean isHasEyePointAsEye() {
        return getEyePointAsEye() != null;
    }

    public void notifyEyePointState() {
        Player player = getPlayer();
        if (player == null) {
            throw new UnknownLogicException("no player" + " uid:" + getPlayerUid());
        }

        ProtoScene.PlayerEyePointStateNtf.Builder notify = ProtoScene.PlayerEyePointStateNtf.newBuilder();
        if (eyePoint == null || SceneUtil.isNotifyEyePointClosed()) {
            notify.setIsUseEyePoint(false);
//#ifdef HK4E_DEBUG
            log.debug("notify_to uid:{} no EyePoint", getPlayerUid());
//#endif
        } else {
//            notify.set_is_use_eye_point(true);
//            proto::Vector proto_pos = *notify.mutable_eyePointpos();
//            proto_pos = eyePoint.getPosition();
//            Region region = eyePoint.getRegion();
//            if (region != null)
//            {
//                notify.set_region_entity_id(region.getEntityId());
//                notify.set_region_group_id(region.getGroupId());
//                notify.set_region_config_id(region.getConfigId());
//                data::RegionShape region_shape = region.getShape();
//                notify.set_region_shape(region_shape);
//                if (region_shape == data::REGION_SPHERE)
//                {
//                    SphereRegion sphere_region = to<SphereRegion>(*region);
//                    if (sphere_region != null)
//                    {
//                        notify.set_sphere_radius(sphere_region.getRadius());
//                    }
//                    else
//                    {
//                        LOG_ERROR + "eye_point wrong region_type. region Desc:" + region.getDesc();
//                    }
//                }
//                else if (region_shape == data::REGION_CUBIC)
//                {
//                    CubicRegion cubic_region = to<CubicRegion>(*region);
//                    if (cubic_region != null)
//                    {
//                        proto::Vector proto_cubic_size = *notify.mutable_cubic_size();
//                        proto_cubic_size = cubic_region.getSize();
//                    }
//                    else
//                    {
//                        LOG_ERROR + "eye_point wrong region_type. region Desc:" + region.getDesc();
//                    }
//                }
//                else
//                {
//                    LOG_ERROR + "eye_point wrong region_type. region Desc:" + region.getDesc();
//                }
//#ifdef HK4E_DEBUG
//                LOG_VIEW_DEBUG("notify_to uid:%u setEyePoint. eyePointentity_id:%u region:%s", getPlayerUid(), eyePoint.getEntityId(), region.getDesc().c_str());
//#endif
//            }
//            else
//            {
//                LOG_ERROR + "uid:" + getPlayerUid() + " eye_point has no related region. Desc:" + eyePoint.getDesc();
//            }
        }
        player.sendMessage(ProtoMsgId.MsgId.PLAYER_EYE_POINT_STATE_NTF, notify.build());
    }

    public Collection<Entity> getEntitiesInView() {
        return viewEntityMap.values();
    }

    public boolean isContainEntityInView(Entity entity) {
        if (entity == null) {
            throw new UnknownLogicException("entity is null");
        }
        return viewEntityMap.containsKey(entity.getEntityId());
    }

    public boolean addEntityInView(Entity entity) {
        if (entity == null) {
            throw new UnknownLogicException("entity is null");
        }

        if (viewEntityMap.put(entity.getEntityId(), entity) == null) {
//#ifdef HK4E_DEBUG
//            log.debug("uid:" + getPlayerUid() + " viewEntityMap size:" + viewEntityMap.size() + " add entity:" + entity.getEntityId());
//#endif
            return true;
        }
        return false;
    }

    public boolean delEntityInView(Entity entity) {
        if (entity == null) {
            throw new UnknownLogicException("entity is null");
        }

        if (viewEntityMap.remove(entity.getEntityId()) != null) {
//#ifdef HK4E_DEBUG
//            log.debug("uid:" + getPlayerUid() + " viewEntityMap size:" + viewEntityMap.size() + " del entity:" + entity.getEntityId());
//#endif
            return true;
        }
        return false;
    }

    public Collection<Entity> getEntitiesInSightRange() {
        return inSightRangeEntityMap.values();
    }

    public boolean isContainEntityInSightRange(Entity entity) {
        if (entity == null) {
            throw new UnknownLogicException("entity is null");
        }
        return inSightRangeEntityMap.containsKey(entity.getEntityId());
    }

    public boolean addEntityInSightRange(Entity entity) {
        if (entity == null) {
            throw new UnknownLogicException("entity is null");
        }

        if (inSightRangeEntityMap.put(entity.getEntityId(), entity) == null) {
//#ifdef HK4E_DEBUG
//            log.debug("uid:" + getPlayerUid() + " inSightRangeEntityMap size:" + inSightRangeEntityMap.size() + " add entity:" + entity.getEntityId());
//#endif
            return true;
        }
        return false;
    }

    public boolean delEntityInSightRange(Entity entity) {
        if (entity == null) {
            throw new UnknownLogicException("entity is null");
        }

        if (inSightRangeEntityMap.remove(entity.getEntityId()) != null) {
//#ifdef HK4E_DEBUG
//            log.debug("uid:" + getPlayerUid() + " inSightRangeEntityMap size:" + inSightRangeEntityMap.size() + " del entity:" + entity.getEntityId());
//#endif
            return true;
        }
        return false;
    }

    public boolean addSightGroupInView(EntitySightGroup sightGroup) {
        if (sightGroup == null) {
            throw new UnknownLogicException("sightGroup is null");
        }

        if (viewSightGroupMap.put(sightGroup.getSelfId(), sightGroup) == null) {
//#ifdef HK4E_DEBUG
            log.debug("uid:" + getPlayerUid() + " viewSightGroupMap size:" + viewSightGroupMap.size() + " add sightGroup:" + sightGroup.getSelfId());
//#endif
            return true;
        }
        return false;
    }

    public boolean delSightGroupInView(EntitySightGroup sightGroup) {
        if (sightGroup == null) {
            throw new UnknownLogicException("sightGroup is null");
        }

        if (viewSightGroupMap.remove(sightGroup.getSelfId()) != null) {
//#ifdef HK4E_DEBUG
            log.debug("uid:" + getPlayerUid() + " viewSightGroupMap size:" + viewSightGroupMap.size() + " del sightGroup:" + sightGroup.getSelfId());
//#endif
            return true;
        }
        return false;
    }

    public boolean isContainGroupVisionType(int type) {
        return groupVisionTypeSet.contains(type);
    }

    public void setGroupVisionTypeSet(Set<Integer> typeSet) {
        if (SceneUtil.isGroupVisionTypeClosed()) {
//#ifdef HK4E_DEBUG
            log.debug("[FEATURE_SWITCH] GroupVisionTypeClosed");
//#endif
            return;
        }

        Player player = getPlayer();
        if (player == null) {
            throw new UnknownLogicException("player is null" + " uid:" + getPlayerUid());
        }

        Scene scene = getScene();
        if (scene == null) {
            throw new UnknownLogicException("scene is null" + " uid:" + getPlayerUid());
        }

        Entity player_eye_entity = scene.getPlayerEyeEntity(getPlayerUid());
        if (player_eye_entity == null) {
            throw new UnknownLogicException("player_eye_entity is null" + " uid:" + getPlayerUid());
        }

        try (PlayerChangeEyePropGuard guard = new PlayerChangeEyePropGuard(this, true)) {
            doSetGroupVisionTypeSet(typeSet);
            scene.refreshPlayerInSightRangeEntitiesAndEnter(player);
        }
    }

    public void resetGroupVisionTypeSet() {
        if (SceneUtil.isGroupVisionTypeClosed()) {
//#ifdef HK4E_DEBUG
            log.debug("[FEATURE_SWITCH] GroupVisionTypeClosed");
//#endif
            return;
        }

        Player player = getPlayer();
        if (player == null) {
            throw new UnknownLogicException("player is null" + " uid:" + getPlayerUid());
        }

        Scene scene = getScene();
        if (scene == null) {
            throw new UnknownLogicException("scene is null" + " uid:" + getPlayerUid());
        }

        Entity player_eye_entity = scene.getPlayerEyeEntity(getPlayerUid());
        if (player_eye_entity == null) {
            throw new UnknownLogicException("player_eye_entity is null" + " uid:" + getPlayerUid());
        }

        try (PlayerChangeEyePropGuard guard = new PlayerChangeEyePropGuard(this, true)) {
            doResetGroupVisionTypeSet();
            scene.refreshPlayerInSightRangeEntitiesAndEnter(player);
        }
    }

    public void doSetGroupVisionTypeSet(Set<Integer> typeSet) {
        log.debug("uid:" + getPlayerUid() + " set group_vision_set from " + groupVisionTypeSet + " to " + typeSet);

        groupVisionTypeSet = typeSet;
    }

    public void doResetGroupVisionTypeSet() {
        log.debug("uid:" + getPlayerUid() + " reset group_vision_set from " + groupVisionTypeSet + " to {DEFAULT}");

        groupVisionTypeSet.clear();
        groupVisionTypeSet.add(ProtoScene.GroupVisionType.GVT_DEFAULT_VALUE);
    }

    public void onTimer() {
//        if (eyePoint != null)
//        {
//            bool need_clear = false;
//            do {
//                GamePlayer player = getPlayer();
//                if (player == null)
//                {
//                    LOG_DEBUG + "player is null" + " uid:" + getPlayerUid();
//                    need_clear = true;
//                    break;
//                }
//
//                Avatar avatar = player.getCurAvatar();
//                if (avatar == null)
//                {
//                    LOG_DEBUG + "avatar is null" + " uid:" + getPlayerUid();
//                    need_clear = true;
//                    break;
//                }
//
//                if (!avatar.isOnScene())
//                {
//                    LOG_DEBUG + "avatar is not onScene" + " uid:" + getPlayerUid() + " avatar:" + *avatar;
//                    need_clear = true;
//                    break;
//                }
//
//                if (!eyePoint.getRelatedBigRegion().isInRegion(avatar.getPosition()))
//                {
//                    LOG_DEBUG + "avatar is not in big region" + " uid:" + getPlayerUid()
//                            + " avatar:" + *avatar + " eye_point:" + *eyePoint;
//                    need_clear = true;
//                    break;
//                }
//            } while(0);
//
//            if (need_clear)
//            {
//                LOG_DEBUG + "eyePoint check failed. clear it. " + " uid:" + getPlayerUid() + " eye_point:" + *eyePoint;
//                clearEyePoint(eyePoint.getRegionEntityId(), false);
//            }
//        }
    }
}
