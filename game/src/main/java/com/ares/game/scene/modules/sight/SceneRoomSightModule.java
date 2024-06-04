package com.ares.game.scene.modules.sight;

import com.ares.common.math.Vector3;
import com.ares.common.util.ForeachPolicy;
import com.ares.game.scene.Grid;
import com.ares.game.scene.Region;
import com.ares.game.scene.Scene;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.visitor.VisitEntityVisitor;
import com.ares.game.scene.visitor.Visitor;
import com.game.protoGen.ProtoScene;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 *  以房间作为视野，只能挂在房间场景上
 *  考虑到后续的房间会像花瓣一样在y轴累加上去,以房间为单位管理entity包括region
 **/
@Slf4j
public class SceneRoomSightModule extends SceneSightModule{
    private static final int LOBBY_ROOM_ID = 1;

    private final Map<Integer, Set<Entity>> roomEntityMap = new HashMap<>();
    private final Map<Integer, Set<Region>> roomRegionMap = new HashMap<>();     // Region对象单独维护
    private final Grid grid = new Grid();

    public SceneRoomSightModule(Scene scene) {
        super(scene);
    }

    @Override
    public Collection<Entity> placeEntity(Entity entity)
    {
        if (0 == entity.getRoomId())
        {
            log.info("force lobby_room_id, entity: " + entity);
            entity.setRoomId(LOBBY_ROOM_ID);
        }
        int roomId = entity.getRoomId();
        entity.setGrid(grid);

        // region对象单独维护
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_REGION)
        {
            Set<Region> regionSet = roomRegionMap.computeIfAbsent(roomId, k -> new HashSet<>());
            regionSet.add((Region) entity);
            return List.of();
        }

        Set<Entity> entitySet = roomEntityMap.computeIfAbsent(roomId, k -> new HashSet<>());
        entitySet.add(entity);

        if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
        {
            return getRoomAvatarList(roomId);
        }
        else
        {
            return getRoomEntityList(entity, roomId);
        }
    }

    @Override
    public Collection<Entity> removeEntity(Entity entity)
    {
        if (0 == entity.getRoomId())
        {
            log.info("force lobby_room_id, entity: " + entity);
            entity.setRoomId(LOBBY_ROOM_ID);
        }
        int roomId = entity.getRoomId();
        entity.setGrid(null);

        // region对象单独维护
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_REGION)
        {
            Set<Region> regionSet = roomRegionMap.computeIfAbsent(roomId, k -> new HashSet<>());
            if (!regionSet.remove((Region) entity))
            {
                log.warn("room: " + roomId + " removeEntity, but not find region: " + entity);
            }
            return List.of();
        }

        Set<Entity> entitySet = roomEntityMap.computeIfAbsent(roomId, k -> new HashSet<>());
        if (!entitySet.remove(entity))
        {
            log.warn("room: " + roomId + " removeEntity, but not find entity: " + entity);
        }

        if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
        {
            return getRoomAvatarList(roomId);
        }
        else
        {
            return getRoomEntityList(entity, roomId);
        }
    }

    // 获取entity周边所有的entity
    private List<Entity> getRoomEntityList(Entity entity, int roomId)
    {
        Set<Entity> entitySet = roomEntityMap.get(roomId);
        if (entitySet == null)
        {
            return List.of();
        }
        VisitEntityVisitor visitor = new VisitEntityVisitor(entity);
        for (Entity entity1 : entitySet)
        {
            entity1.accept(visitor);
        }
        return visitor.getResultList();
    }

    private List<Entity> getRoomAvatarList(int roomId)
    {
        List<Entity> result = new ArrayList<>();
        getScene().foreachPlayer(player ->{
            Avatar avatar = player.getCurAvatar();
            if (avatar != null && avatar.isOnScene() && roomId == avatar.getRoomId())
            {
                result.add(avatar);
            }
            return ForeachPolicy.CONTINUE;
        });
        return result;
    }

    @Override
    public EntityMoveToRet entityMoveTo(Entity entity, Vector3 dest_pos)
    {
        // 无法从pos中计算出room_id, avatar的room_id变化由专门的协议同步
        // 按照设定，房间场景其他entity不会出单个房间
        return null;
    }

    // 查询所有视距级别，entity附近满足visitor条件的entity集合
    @Override
    public void visitGridsInSight(Entity entity, Visitor visitor)
    {
        int roomId = entity.getRoomId();
        innerVisitGridsInSight(roomId, visitor);
    }

    private void innerVisitGridsInSight(int room_id, Visitor visitor)
    {
        Set<Entity> entitySet = roomEntityMap.get(room_id);
        if (entitySet == null) {
            return;
        }
        for (Entity entity : entitySet) {
            entity.accept(visitor);
        }
    }

    // entity是否移动格子
    @Override
    public boolean isEntityMoveGrid(Entity entity, Vector3 prev_pos, int prevRoom)
    {
        return entity.getRoomId() != prevRoom;
    }

    // 寻找pos关联的region列表
    @Override
    public Set<Region> findPossibleRegionSet(Vector3 pos, int roomId)
    {
        Set<Region> regionSet = innerFindPossibleRegionSet(roomId);
        if (roomId != 0)
        {
            regionSet.addAll(innerFindPossibleRegionSet(0));
        }

        return regionSet;
    }

    private Set<Region> innerFindPossibleRegionSet(int roomId)
    {
        return roomRegionMap.get(roomId);
    }

    public EntityMoveToRet entityMoveRoom(Entity entity, int destRoomId)
    {
        EntityMoveToRet ret = new EntityMoveToRet();
        int curRoomId = entity.getRoomId();
        if (curRoomId == destRoomId)
        {
            ret.missEntities = List.of();
            ret.meetEntities = List.of();
            return ret;
        }

        // 切换room_id记录
        Set<Entity> curEntitySet = roomEntityMap.computeIfAbsent(curRoomId, k -> new HashSet<>());
        if (!curEntitySet.remove(entity))
        {
            log.warn("entity: " + entity + " not in curRoomId: " + curRoomId);
        }
        Set<Entity> destEntitySet = roomEntityMap.computeIfAbsent(destRoomId, k -> new HashSet<>());
        destEntitySet.add(entity);
        entity.setRoomId(destRoomId);

        if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
        {
            ret.missEntities = getRoomAvatarList(curRoomId);
            ret.meetEntities = getRoomAvatarList(destRoomId);
        }
        else
        {
            ret.missEntities = getRoomEntityList(entity, curRoomId);
            ret.meetEntities = getRoomEntityList(entity, destRoomId);
        }

        return ret;
    }
}
