package com.ares.game.scene.subclass;

import com.ares.common.math.Coordinate;
import com.ares.common.math.Vector3;
import com.ares.game.player.Player;
import com.ares.game.scene.PlayerViewMgr;
import com.ares.game.scene.Scene;
import com.ares.game.scene.VisionContext;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.modules.sight.SceneRoomSightModule;
import com.ares.game.scene.modules.sight.SceneSightModule;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Slf4j
public abstract class RoomScene extends Scene {
    private long lastLocationNotifyTime;
    private SceneRoomSightModule roomSightModule = new SceneRoomSightModule(this);

    public RoomScene(int sceneId) {
        super(sceneId);
    }

    public void entityMoveRoom(Entity entity, int roomId)
    {
        // 不能走entityMoveTo，move协议不含有room_id
        int prevRoom = entity.getRoomId();

        SceneRoomSightModule room_sight_comp = getRoomSightModule();
        SceneSightModule.EntityMoveToRet moveToRet = room_sight_comp.entityMoveRoom(entity, roomId);
        Set<Long> missUidSet = getPlayerUidSetByViewEntity(moveToRet.missEntities, 0);
        Set<Long> meetUidSet = getPlayerUidSetByViewEntity(moveToRet.meetEntities, 0);

        entityLeavePlayersSightRange(missUidSet, entity, VisionContext.MISS);
        entityEnterPlayersSightRange(meetUidSet, entity, VisionContext.MEET);
        do {
            if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
            {
                break;
            }

            Player player = entity.getPlayer();
            if (player == null)
            {
                break;
            }

            PlayerViewMgr viewMgr = findPlayerViewMgr(player.getUid());
            if (viewMgr == null)
            {
                log.error("uid:{} view_mgr is null", player.getUid());
                break;
            }

            if (viewMgr.isHasEyePointAsEye())
            {
                break;
            }

            entitiesLeavePlayerSightRange(player, moveToRet.missEntities, VisionContext.MISS);
            entitiesEnterPlayerSightRange(player, moveToRet.meetEntities, VisionContext.MEET);

        } while(false);

        checkRoomRegion(entity.getPosition(), ProtoScene.VisionType.VISION_NONE, prevRoom, entity);
//
//        CheckRegionParam check_param(CHECK_REGION_MOVE);
//        check_param.prev_pos = entity.getPosition();
//        check_param.prevRoom = prevRoom;
//        checkEnterWeatherArea(entity, proto::VISION_NONE, check_param);
    }

    private void checkRoomRegion(Vector3 prevPos, ProtoScene.VisionType visionType, int prevRoom, Entity entity)
    {
        log.debug("room:" + prevRoom + " . " + entity.getRoomId() + " " + entity);
        if (!entity.canEnterRegion() || entity.getRoomId() == prevRoom)
        {
            return;
        }

//        bool is_trigger_enter_leave_evt = Region::isTriggerEnterLeaveEvtByVisionType(visionType);

        // 退出旧房间region
//        SceneSightModule sight_comp = getSightModule();
//        Set<Region> prev_region_set = sight_comp.findPossibleRegionSet(prevPos, prevRoom);
//        for (Region region : prev_region_set)
//        {
//            if (region != null && region.isEntityInRegion(entity.getEntityId()))
//            {
//                LOG_DEBUG + "[REGION] moveRoom entity is exiting region:" + *region + " " + entity;
//                region.delEntity(entity, is_trigger_enter_leave_evt);
//            }
//        }
//
//        // 进入新房间region
//        Set<Region> region_set = sight_comp.findPossibleRegionSet(entity.getPosition(), entity.getRoomId());
//        for (auto region : region_set)
//        {
//            //LOG_DEBUG + "region in new room:" + *region;
//            if (region != null && region.isInRegion(entity.getPosition()))
//            {
//                LOG_DEBUG + "[REGION] moveRoom entity is entering region:" + *region + " " + entity;
//                region.addEntity(entity, is_trigger_enter_leave_evt);
//            }
//        }
    }

    // 定时器回调
    void onTimer(long now_ms)
    {
//        Scene::onTimer(now_ms);
//
//        int now = now_ms / 1000;
//        if (now > last_location_notify_time_ + 5)
//        {
//            last_location_notify_time_ = now;
//            notifyAllPlayerLocation();
//        }
    }

    // 玩家进入场景
    @Override
    public void playerEnter(Player player, Vector3 pos, Vector3 rot, boolean reLogin, List<Avatar> enterSceneAvatarList, Avatar appearAvatar)
    {
        super.playerEnter(player, pos, rot, reLogin, enterSceneAvatarList, appearAvatar);
        Coordinate coordinate;
        float height = 0;
//        if (0 == GET_JSON_CONFIG_MGR.findRoomCoordinateInWorld(scene_id_, coordinate, height))
//        {
//            PlayerWorldScene world_scene = player.getSceneModule().findMainWorldScene();
//            if (null != world_scene)
//            {
//                WeatherArea prev_weather_area = player.getSceneModule().getWeatherArea();
//                if (prev_weather_area != null)
//                {
//                    prev_weather_area.leaveWeatherArea(player);
//                }
//                WeatherArea weather_area = world_scene.findWeatherAreaByCoordAndHeight(coordinate, height);
//                if (null != weather_area)
//                {
//                    weather_area.enterWeatherArea(player);
//                }
//                else
//                {
//                    LOG_DEBUG + "no weather area in coordinate:" + coordinate.toString() + " player:" + player;
//                }
//            }
//            else
//            {
//                LOG_WARNING + "world_scene is null, player:" + player;
//            }
//        }
    }

    // 玩家离开场景
    @Override
    public void playerLeave(long uid)
    {
        Player player = findPlayer(uid);
        if (player != null)
        {
//            WeatherArea prev_weather_area = player.getSceneModule().getWeatherArea();
//            if (prev_weather_area != null)
//            {
//                prev_weather_area.leaveWeatherArea(*player);
//            }
        }
        else
        {
            log.warn("findPlayer failed, uid:" + uid + " scene_id:" + getSceneId());
        }
        super.playerLeave(uid);
    }
}
