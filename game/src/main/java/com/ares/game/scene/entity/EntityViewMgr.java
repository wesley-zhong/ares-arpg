package com.ares.game.scene.entity;

import com.ares.game.player.Player;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class EntityViewMgr {
    private ProtoScene.GroupVisionType groupVisionType = ProtoScene.GroupVisionType.GVT_ALWAYS_SHOW;
    private Map<Long, Player> viewingPlayerMap = new HashMap<>();    // 查看该entity的player集合 uid.player_wtr
    EntitySightGroup sightGroup;   // 当前所在的sight_group
    EntitySightGroup preinstallSightGroup;  // enter_scene前预设，enter_scene时将进入的sight_group

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
    }

    public void onPlayerUndoView(Player player)
    {
        if (player == null)
        {
            throw new NullPointerException("player is null");
        }
        viewingPlayerMap.remove(player.getUid());
        log.debug("viewing_player_map_ size:" + viewingPlayerMap.size() + " del uid:" + player.getUid());
    }
}
