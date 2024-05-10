package com.ares.game.scene.entity;

import com.ares.game.player.Player;
import com.ares.game.scene.entity.creature.Creature;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Slf4j
public class EntitySightGroup {
    private static final AtomicInteger nextId = new AtomicInteger(1);

    private int selfId;
    private ProtoScene.GroupVisionType groupVisionType = ProtoScene.GroupVisionType.GVT_ALWAYS_SHOW;
    private Map<Integer, Entity> entityMap = new HashMap<>();
    private Map<Long, Player> viewingPlayerMap = new HashMap<>();    // 查看该group的player集合 uid.player_wtr
    private int authorityPeerId = 0;

    public EntitySightGroup()
    {
        selfId = nextId.getAndIncrement();
        if (selfId == 0) {
            selfId = nextId.getAndIncrement();
        }
    }

    public Collection<Entity> getEntitiesInSightGroup()
    {
        return entityMap.values();
    }

    public void addEntityInSightGroup(Entity entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("entity is null");
        }
        entityMap.put(entity.getEntityId(), entity);
        entity.setSightGroup(this);
        if (entity instanceof Creature creature)
        {
//            creature.onAuthorityChangedToPeerID(authority_peer_id_, creature.getViewMgr().validateAndGetViewingPlayers());
        }
        log.debug("sight_group:" + getSelfId() + " entityMap size:" + entityMap.size() + " add entity:" + entity.getEntityId());
    }

    public void delEntityInSightGroup(Entity entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("entity is null");
        }
        entityMap.remove(entity.getEntityId());
        entity.setSightGroup(null);
        log.debug("sight_group:" + getSelfId() + " entityMap size:" + entityMap.size() + " del entity:" + entity.getEntityId());
    }

    public Collection<Player> getViewingPlayers()
    {
        return viewingPlayerMap.values();
    }

    public void onPlayerDoView(Player player)
    {
        if (player == null)
        {
            throw new NullPointerException("player is null");
        }
        viewingPlayerMap.put(player.getUid(), player);
        log.debug("sight_group:" + getSelfId() + " viewingPlayerMap size:" + viewingPlayerMap.size() + " add uid:" + player.getUid());
    }

    public void onPlayerUndoView(Player player)
    {
        if (player == null)
        {
            throw new NullPointerException("player is null");
        }
        viewingPlayerMap.remove(player.getUid());
        log.debug("sight_group:" + getSelfId() + " viewingPlayerMap size:" + viewingPlayerMap.size() + " del uid:" + player.getUid());
    }

    public void onEnterPlayerView(Player player)
    {
        onPlayerDoView(player);
        // 当无主机时进行分配
        if (authorityPeerId == 0)
        {
            // 后续的appear会通知
            onAuthorityChangedToPeerID(player.getSceneModule().getPeerId(), List.of(), false);
        }
    }

    public void onExitPlayerView(Player player)
    {
        onPlayerUndoView(player);
        if (authorityPeerId == player.getSceneModule().getPeerId())
        {
            refreshAuthority(null, false);
        }
    }

    // 刷新主机
    private void refreshAuthority(Player authority_player, boolean delay_sync)
    {
        log.debug("sight_group:" + getSelfId() + " refreshAuthority");
//
//        vector<GamePlayer> player_vec = validateAndGetViewingPlayers();
//        // 未指定主机, 按标准筛选
//        if (authority_player == null)
//        {
//            std::vector<GamePlayer> candidate_player_vec;
//            for (GamePlayer player : player_vec)
//            {
//                // 必要条件
//                if (player.isConnected())
//                {
//                    candidate_player_vec.emplace_back(player);
//                }
//            }
//            auto sort_func = [](GamePlayer a, GamePlayer b) . bool
//            {
//                if (a == null)
//                {
//                    return false;
//                }
//                if (b == null)
//                {
//                    return true;
//                }
//                if (!a.getIsValidForAuthority())
//                {
//                    return false;
//                }
//                if (!b.getIsValidForAuthority())
//                {
//                    return true;
//                }
//                return a.getRtt() < b.getRtt();
//            };
//            std::sort(candidate_player_vec.begin(), candidate_player_vec.end(), sort_func);
//            if (!candidate_player_vec.empty())
//            {
//                authority_player = candidate_player_vec[0];
//            }
//        }
//        onAuthorityChangedToPlayer(authority_player, player_vec, delay_sync);
    }

    public void onAuthorityChangedToPlayer(Player authority_player, Collection<Player> notify_player_vec, boolean delay_sync)
    {
        int new_peer_id = authority_player != null ? authority_player.getSceneModule().getPeerId() : 0;
        onAuthorityChangedToPeerID(new_peer_id, notify_player_vec, delay_sync);
    }

    public void onAuthorityChangedToPeerID(int new_peer_id, Collection<Player> notify_player_vec, boolean delay_sync)
    {
        // 无变化不切换
        if (new_peer_id == authorityPeerId)
        {
            return;
        }

        log.debug("authority_peer_id:" + authorityPeerId + "." + new_peer_id);

        setAuthorityPeerId(new_peer_id);

//        for (auto iter = entity_map_.begin(); iter != entity_map_.end();)
//        {
//            Entity entity = iter.second.lock();
//            if (entity != null)
//            {
//                Creature creature = to<Creature>(*entity);
//                if (creature != null)
//                {
//                    creature.onAuthorityChangedToPeerID(new_peer_id, notify_player_vec, true);
//                }
//                ++iter;
//            }
//            else
//            {
//                LOG_WARNING("entity_id:%u weak expired", iter.first);
//                iter = entity_map_.erase(iter);
//            }
//        }
//        if (!delay_sync)
//        {
//            GAME_THREAD_LOCAL.player_send_packet_controller.forceFlushAll();
//        }
    }
}
