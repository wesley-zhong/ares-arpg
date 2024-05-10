package com.ares.game.scene;

import com.ares.common.util.ForeachPolicy;
import com.ares.core.excetion.UnknownLogicException;
import com.ares.game.player.Player;
import com.ares.game.scene.entity.avatar.Avatar;
import com.game.protoGen.ProtoAvatar;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
@Setter
@Slf4j
public class SceneTeam {
    @Getter
    static class SceneTeamAvatar
    {
        private long uid;
        private long avatarGuid;
        private Avatar avatar;

        public SceneTeamAvatar() {}
        public SceneTeamAvatar(long uid, long avatar_guid, Avatar avatar) {
            this.uid = uid;
            this.avatarGuid = avatar_guid;
            this.avatar = avatar;
        }

        void toClient(ProtoAvatar.SceneTeamAvatar.Builder pb, boolean mpMode) {
        }
    }

    private final List<Long> playerUidList = new ArrayList<>();
    private final Map<Long, Player> playerMap = new HashMap<>();
    private final Map<Long, List<SceneTeamAvatar>> teamAvatarMap = new HashMap<>(); // uid . [avatar_1, avatar_2]
    boolean inMpMode = false;     // 是否在Mp模式中
    private long hostUid = 0;
    private int sceneId = 0;

    public void init(Player hostPlayer, long hostUid, int sceneId)
    {
        this.hostUid = hostUid;
        this.sceneId = sceneId;
        if (hostPlayer != null)
        {
            playerMap.put(hostUid, hostPlayer);
        }
    }

    public static int getAllowAvatarNum(int teamSize, boolean host)
    {
        // 4 人: 1 + 1 + 1 + 1
        // 3 人: 2 + 1 + 1
        // 2 人: 2 + 2
        // 1 人: 4 人 临时上阵可能会有很多人, 先写10人了.
        switch (teamSize)
        {
            case 1:
                // 临时上阵可能会多于4个...
                return 10;
            case 2:
                return 2;
            case 3:
                return host ? 2 : 1;
            case 4:
                return 1;
            default:
                return 0;
        }
    }

    public void onPlayerEnter(Player player)
    {
        long uid = player.getUid();
        if (teamAvatarMap.containsKey(uid)) {
            return;
        }
        // 是房主的时候保护一下, 保证一定加在头部
        // 保证房主一定是1P
        if (uid == getHostUid())
        {
//#ifdef HK4E_DEBUG
            if (!playerUidList.isEmpty())
            {
                log.debug("[SCENE_TEAM] host enter scene team after other, host_uid: " + uid + " playerUidList:" + playerUidList);
            }
//#endif
            playerUidList.add(0, uid);
        }
        else
        {
            playerUidList.add(uid);
        }
        teamAvatarMap.put(uid, new ArrayList<>());
        playerMap.put(uid, player);
    }

    public void onPlayerLeave(long uid)
    {
        Player player = findPlayer(uid);
        if (null == player)
        {
            return;
        }
        if (!teamAvatarMap.containsKey(uid)) {
            return;
        }
        // 结算
        List<SceneTeamAvatar> oldAvatarList = getAllSceneTeamAvatarList();
        // 删除对应UID
        playerUidList.remove(uid);
        teamAvatarMap.remove(uid);
        playerMap.remove(uid);
        // 天赋效果改变 TODO 每5分钟一次的toBin需要去掉这种加成
        onSceneTeamChange(oldAvatarList);
//         打日志并通知剩下的人
//        logChangeSceneTeam(*player, oldAvatarList, proto_log::CHANGE_SCENE_TEAM_REASON_MP_PLAYER_LEAVE);
        notifySceneTeamUpdate();
    }

    public boolean isPlayerSceneTeamIdenticalToThis(long uid)
    {
        Player player = findPlayer(uid);
        if (null == player)
        {
            return false;
        }
        // 使用指针判断是否是同一个队伍
        return player.getAvatarModule().findSceneTeam() == this;
    }

    public int getTeamPlayerNum()
    {
        return playerMap.size();
    }

    private void resizeAvatarTeam()
    {
        for (Map.Entry<Long, List<SceneTeamAvatar>> entry : teamAvatarMap.entrySet())
        {
            long uid = entry.getKey();
            List<SceneTeamAvatar> avatarList = entry.getValue();
            int allow_size = getAllowAvatarNum(uid);
            if (avatarList.size() <= allow_size)
            {
                continue;
            }
//            // 需要resize
//            Player player = findPlayer(uid);
//            if (player == null)
//            {
//                log.ERROR + "Player is null, uid:" + uid;
//                avatarList.resize(allow_size);
//                continue;
//            }
//            auto old_scene_avatar_vec = avatarList;
//            avatarList.clear();
//            Avatar cur_avatar = player.getAvatarComp().getCurAvatar();
//            // 第一遍找cur_avatar
//            for (auto scene_avatar : old_scene_avatar_vec)
//            {
//                if (cur_avatar != null && scene_avatar.getAvatarGuid() == cur_avatar.getGuid())
//                {
//                    avatarList.emplace_back(scene_avatar);
//                }
//            }
//            // 后面塞到够为止
//            for (auto scene_avatar : old_scene_avatar_vec)
//            {
//                if (cur_avatar != null && scene_avatar.getAvatarGuid() == cur_avatar.getGuid())
//                {
//                    continue;
//                }
//                if (avatarList.size() >= allow_size)
//                {
//                    break;
//                }
//                avatarList.emplace_back(scene_avatar);
//            }
//            if (isPlayerSceneTeamIdenticalToThis(player.getUid()))
//            {
//                // 清除被离开SceneTeam的角色的AbilityComp信息，防止重新加上来的时候带AbilitySyncInfo TODO 统一一下清Ability的方式
//                List<long> scene_avatar_guid_vec;
//                for (SceneTeamAvatar scene_team_avatar : avatarList)
//                {
//                    scene_avatar_guid_vec.push_back(scene_team_avatar.getAvatarGuid());
//                }
//                for (SceneTeamAvatar scene_team_avatar : old_scene_avatar_vec)
//                {
//                    if (MiscUtils::isContains(scene_avatar_guid_vec, scene_team_avatar.getAvatarGuid()))
//                    {
//                        continue;
//                    }
//                    Avatar avatar = scene_team_avatar.getAvatar();
//                    if (avatar == null)
//                    {
//                        log.WARNING + "avatar is null, uid:" + uid;
//                        continue;
//                    }
//                    Scene scene = avatar.getScene();
//                    if (scene == null)
//                    {
//                        log.WARNING + "getScene fail, avatar:" + *avatar;
//                        continue;
//                    }
//                    scene.delAvatarAndWeaponEntity(*avatar);
//                }
//            }
        }
    }

    // 获取玩家允许的角色数
    private int getAllowAvatarNum(long uid)
    {
        if (!teamAvatarMap.containsKey(uid)) {
            return 0;
        }
        return getAllowAvatarNum(getTeamPlayerNum(), uid == getHostUid());
    }

    // 设置玩家的角色队伍
    public void setPlayerAvatarTeam(long uid, List<Long> guidList, long appearAvatarGuid, ProtoScene.ChangeSceneTeamReason reason, boolean notify)
    {
        if (teamAvatarMap.get(uid) == null)
        {
            throw new UnknownLogicException("set avatar team failed. uid not in team:" + uid);
        }
        Player player = findPlayer(uid);
        if (null == player)
        {
            throw new UnknownLogicException("set avatar team failed. get player failed:" + uid);
        }
        List<Long> realGuidList = new ArrayList<>();
        if (getAllowAvatarNum(uid) < guidList.size())
        {
            log.warn("set avatar team size too big. set_size:" + guidList.size() + ", allow:" + getAllowAvatarNum(uid));
        }
        for (int i = 0; i < Math.min(guidList.size(), getAllowAvatarNum(uid)); i++) {
            realGuidList.add(guidList.get(i));
        }

        if (realGuidList.isEmpty())
        {
            realGuidList.add(appearAvatarGuid);
        }
        else if (!realGuidList.contains(appearAvatarGuid))
        {
            realGuidList.removeLast();
            realGuidList.add(appearAvatarGuid);
        }
        List<SceneTeamAvatar> allOldAvatarList = getAllSceneTeamAvatarList();
        List<SceneTeamAvatar> avatarList = new ArrayList<>();
        for (long guid : realGuidList)
        {
            Avatar avatar = player.getAvatarModule().findAvatar(guid);
            if (avatar == null)
            {
                throw new UnknownLogicException("set avatar team fail. avatar not exist:" + guid);
            }
            avatarList.add(new SceneTeamAvatar(uid, avatar.getGuid(), avatar));
        }

        teamAvatarMap.put(uid, avatarList);
        resizeAvatarTeam();

        // 天赋效果改变
        onSceneTeamChange(allOldAvatarList);
        // 打日志
//        logChangeSceneTeam(*player, allOldAvatarList, reason);
        // 同步
        if (notify)
        {
            notifySceneTeamUpdate();
        }
    }
//
//    void logChangeSceneTeam(Player player, List<SceneTeamAvatar> old_avatar_vec, proto_log::ChangeSceneTeamReason reason)
//    {
//        StatLogUtils::ContextHolder holder(proto_log::PLAYER_ACTION_CHANGE_SCENE_TEAM, player.getBasicComp().getNextTransNo());
//        std::shared<proto_log::PlayerLogBodyChangeSceneTeam> log  = MAKE_SHARED<proto_log::PlayerLogBodyChangeSceneTeam>();
//        if (log == null)
//        {
//            log.ERROR + "get log  failed";
//            return;
//        }
//        List<SceneTeamAvatar> cur_avatar_vec = getAllSceneTeamAvatarList();
//        for (auto team_avatar : old_avatar_vec)
//        {
//            long player_uid = team_avatar.getPlayerUid();
//            long avatar_guid = team_avatar.getAvatarGuid();
//            auto find_pred = [player_uid, avatar_guid](auto  team_avatar)
//            {
//                return team_avatar.getPlayerUid() == player_uid && team_avatar.getAvatarGuid() == avatar_guid;
//            };
//            if (std::find_if(cur_avatar_vec.begin(), cur_avatar_vec.end(), find_pred) != cur_avatar_vec.end())
//            {
//                continue;
//            }
//            Avatar avatar = team_avatar.getAvatar();
//            if (avatar != null)
//            {
//                avatar.getTeamAvatarLog(*log.add_removed_avatar_list());
//            }
//        }
//        for (auto team_avatar : cur_avatar_vec)
//        {
//            long player_uid = team_avatar.getPlayerUid();
//            long avatar_guid = team_avatar.getAvatarGuid();
//            auto find_pred = [player_uid, avatar_guid](auto  team_avatar)
//            {
//                return team_avatar.getPlayerUid() == player_uid && team_avatar.getAvatarGuid() == avatar_guid;
//            };
//            if (std::find_if(old_avatar_vec.begin(), old_avatar_vec.end(), find_pred) != old_avatar_vec.end())
//            {
//                continue;
//            }
//            Avatar avatar = team_avatar.getAvatar();
//            if (avatar != null)
//            {
//                avatar.getTeamAvatarLog(*log.add_added_avatar_list());
//            }
//        }
//        for (auto team_avatar : cur_avatar_vec)
//        {
//            Avatar avatar = team_avatar.getAvatar();
//            if (avatar != null)
//            {
//                avatar.getTeamAvatarLog(*log.add_cur_avatar_list());
//            }
//        }
//        log.set_level1_area_id(player.getSceneComp().getLevel1AreaId());
//        log.set_level2_area_id(player.getSceneComp().getLevel2AreaId());
//        log.set_is_in_mp(isInMpMode());
//        log.set_change_reason(reason);
//        player.printStatLog(log);
//    }

    public void setPlayerAvatarTeamAndAddToScene(long uid, List<Long> avatarGuidList, long appearAvatarGuid, Scene scene, ProtoScene.ChangeSceneTeamReason reason, boolean notify)
    {
        setPlayerAvatarTeam(uid, avatarGuidList, appearAvatarGuid, reason, false);
        int ret = foreachAvatar(uid, avatar -> {
            scene.addAvatarAndWeaponEntity(avatar, true);
            return ForeachPolicy.CONTINUE;
        });
        if (ret != 0)
        {
            throw new UnknownLogicException("set avatar team fail. ret:" + ret);
        }

        // avatar初始化成功通知
//        Player player = GAME_THREAD_LOCAL.player_mgr.findOnlinePlayer(uid);
//        if (player == null)
//        {
//            log.WARNING + "findOnlinePlayer failed, uid:" + uid;
//            return -1;
//        }
//        BaseEvent event = MAKE_SHARED<SetAvatarTeamToSceneEvent>(uid);
//        player.getEventComp().notifyEvent(event);

        if (notify)
        {
            notifySceneTeamUpdate();
        }
    }

    public boolean isAvatarInTeam(long uid, long guid)
    {
        List<SceneTeamAvatar> avatarList = teamAvatarMap.get(uid);
        if (null == avatarList) {
            return false;
        }

        for (SceneTeamAvatar teamAvatar : avatarList)
        {
            if (teamAvatar.getAvatarGuid() == guid) {
                return true;
            }
        }
        return false;
    }

    // 一些遍历用方法
    int foreachAvatar(Function<Avatar, ForeachPolicy> func)
    {
        int ret = 0; // 为 1 表示break
        for (long uid : playerUidList)
        {
            List<SceneTeamAvatar> avatarList = teamAvatarMap.get(uid);
            if (null == avatarList)
                continue;

            for (SceneTeamAvatar teamAvatar : avatarList)
            {
                Avatar avatar = teamAvatar.getAvatar();
                if (null == avatar)
                {
                    continue;
                }
                if (func.apply(avatar) != ForeachPolicy.CONTINUE)
                {
                    ret = 1;
                    break;
                }
            }
            if (ret != 0)
            {
                break;
            }
        }
        return ret;
    }

    public List<Long> getAvatarGuidList(long uid)
    {
        List<SceneTeamAvatar> avatarList = teamAvatarMap.get(uid);
        if (null == avatarList) {
            return List.of();
        }

        List<Long> result = new ArrayList<>();
        for (SceneTeamAvatar teamAvatar : avatarList)
        {
            result.add(teamAvatar.avatarGuid);
        }
        return result;
    }

    List<SceneTeamAvatar> getAllSceneTeamAvatarList()
    {
        List<SceneTeamAvatar> resultList = new ArrayList<>();
        for (long uid : playerUidList)
        {
            List<SceneTeamAvatar> l = teamAvatarMap.get(uid);
            if (null != l) {
                resultList.addAll(l);
            }
        }
        return resultList;
    }

    Map<Long, List<Long>> getPlayerAvatarGuidMap()
    {
        Map<Long, List<Long>> result = new HashMap<>();
        for (Map.Entry<Long, List<SceneTeamAvatar>> entry : teamAvatarMap.entrySet())
        {
            long uid = entry.getKey();
            List<Long> guidList = new ArrayList<>();
            for (SceneTeamAvatar teamAvatar : entry.getValue()) {
                guidList.add(teamAvatar.avatarGuid);
            }
            result.put(uid, guidList);
        }
        return result;
    }

    int foreachAvatar(long uid, Function<Avatar, ForeachPolicy> func)
    {
        List<SceneTeamAvatar> avatarList = teamAvatarMap.get(uid);
        if (null == avatarList) {
            log.error("uid:" + uid + " cannot be found in teamAvatarMap");
            return -1;
        }

        for (SceneTeamAvatar teamAvatar : avatarList)
        {
            Avatar avatar = teamAvatar.getAvatar();
            if (null == avatar)
            {
                continue;
            }
            if (func.apply(avatar) != ForeachPolicy.CONTINUE)
            {
                return 1;
            }
        }

        return 0;
    }

    boolean isInMpMode()
    {
        if (inMpMode)
        {
            return true;
        }
        return findPlayer(getHostUid()) == null || getTeamPlayerNum() > 1;
    }


    void sendProto(ProtoCommon.MsgId msgId, Message proto)
    {
        for (long uid : playerUidList)
        {
            if (uid == 0)
            {
                continue;
            }
            if (isPlayerSceneTeamIdenticalToThis(uid))
            {
                Player player = findPlayer(uid);
                if (player == null)
                {
                    log.warn("cannot find player, uid: " + uid);
                    continue;
                }
                player.sendMessage(msgId, proto);
            }
        }
    }

    // 更新相关通知
    void notifySceneTeamUpdate()
    {
//        proto::SceneTeamUpdateNotify notify;
//        for (long uid : player_uid_vec_)
//        {
//            auto iter = team_avatar_map_.find(uid);
//            if (iter != team_avatar_map_.end())
//            {
//                for (auto team_avatar : iter.second)
//                {
//                    // 加个报警日志
//                    if (team_avatar.getAvatar() == null)
//                    {
//                        log.ERROR + "team_avatar is null, uid:" + team_avatar.getPlayerUid()
//                                + "guid: " + team_avatar.getAvatarGuid();
//                    }
//                    team_avatar.toClient(*notify.add_scene_team_avatar_list(), isInMpMode());
//
//                    auto avatar = team_avatar.getAvatar();
//                    if (avatar)
//                    {
//                        log.DEBUG + "notify avatar: " + avatar.getAvatarId();
//                    }
//                }
//            }
//            Player player = findPlayer(uid);
//            if (null != player)
//            {
//                player.getAvatarComp().setIsReconnectFlag(false);
//            }
//        }
//        if (isInMpMode())
//        {
//            notify.set_is_in_mp(true);
//        }
//        sendProto(notify);
    }

    Player findPlayer(long uid)
    {
        return playerMap.get(uid);
    }

    // 队伍变化导致的ability变化由SceneTeamUpdateNotify发送
    void onSceneTeamChange(List<SceneTeamAvatar> oldAvatarList)
    {
//        FightPropGuard fight_prop_guard;    // 延迟统一计算属性
//        RefreshAbilityGuard refresh_ability_guard;

        // 计算并集
//        Set<Avatar> unionAvatarSet = new HashSet<>();
//        for (SceneTeamAvatar sceneTeamAvatar : oldAvatarList)
//        {
//            Avatar avatar = sceneTeamAvatar.getAvatar();
//            if (null == avatar)
//            {
//                log.warn("getAvatar fail. uid:" + sceneTeamAvatar.getUid()
//                        + " avatar_guid:" + sceneTeamAvatar.getAvatarGuid());
//                continue;
//            }
//            unionAvatarSet.add(avatar);
//        }
//        foreachAvatar(avatar -> {
//            unionAvatarSet.add(avatar);
//            return ForeachPolicy.CONTINUE;
//        });
        //处理天赋
//        for (AvatarWtr avatar_wtr : unionAvatarSet)
//        {
//            Avatar avatar = avatar_wtr.lock();
//            if (null != avatar)
//            {
//                fight_prop_guard.addCreature(*avatar);
//                refresh_ability_guard.addCreature(*avatar);
//                avatar.getTalentComp().onSceneTeamChange();
//            }
//        }

        // 计算差集
        List<SceneTeamAvatar> newAvatarList = getAllSceneTeamAvatarList();
        List<SceneTeamAvatar> diffRemoveAvatarList = new ArrayList<>();
        List<SceneTeamAvatar> diffAddAvatarList = new ArrayList<>();
        for (SceneTeamAvatar sceneTeamAvatar : oldAvatarList)
        {
            if (!isAvatarInTeam(sceneTeamAvatar.getUid(), sceneTeamAvatar.getAvatarGuid()))
            {
                diffRemoveAvatarList.add(sceneTeamAvatar);
            }
        }
        for (SceneTeamAvatar newAvatar : newAvatarList)
        {
            boolean find = false;
            for (SceneTeamAvatar old : oldAvatarList) {
                if (newAvatar.getUid() == old.getUid()
                        && newAvatar.getAvatarGuid() == old.getAvatarGuid()) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                diffAddAvatarList.add(newAvatar);
            }
        }
//#ifdef HK4E_DEBUG
        log.debug("[SCENE_TEAM_CHANGE] diffRemoveAvatarList:" + diffRemoveAvatarList
                + " diffAddAvatarList:" + diffAddAvatarList);
//#endif
        // 处理队伍Buff
        for (SceneTeamAvatar sceneTeamAvatar : diffRemoveAvatarList)
        {
            Avatar avatar = sceneTeamAvatar.getAvatar();
            if (avatar != null)
            {
                avatar.onRemoveFromSceneTeam();
            }
        }
        for (SceneTeamAvatar sceneTeamAvatar : diffAddAvatarList)
        {
            Avatar avatar = sceneTeamAvatar.getAvatar();
            if (avatar != null)
            {
                avatar.onAddToSceneTeam();
            }
        }

        // 处理队伍共鸣
        refreshTeamResonances(diffRemoveAvatarList);
    }

    // 刷一遍队伍共鸣（阵容发生变化）
    void refreshTeamResonances(List<SceneTeamAvatar> diff_remove_avatar_vec)
    {
//        // 计算当前阵容的队伍共鸣
//        List<data::ElementType> elem_vec;
//        long total_promote_level = 0;
//        for (long uid : player_uid_vec_)
//        {
//            // 不在当前场景的玩家不能加队伍共鸣
//            if (!isPlayerSceneTeamIdenticalToThis(uid))
//            {
//                continue;
//            }
//            // 加个保底，排除掉当前场景是null的玩家
//            Player player = findPlayer(uid);
//            if (null == player)
//            {
//                continue;
//            }
//            Scene scene = player.getSceneComp().getCurScene();
//            if (null == scene)
//            {
//                continue;
//            }
//            foreachAvatar(uid, [elem_vec, total_promote_level](Avatar  avatar)
//                {
//            // 试用角色不参与元素共鸣
//            if (avatar.getAvatarType() == proto::AVATAR_TYPE_TRIAL)
//            {
//                return FOREACH_CONTINUE;
//            }
//            elem_vec.push_back(avatar.getElemType());
//            total_promote_level += avatar.getPromoteLevel();
//            return FOREACH_CONTINUE;
//        });
//        }
//        List<long> team_resonance_id_vec = GET_TXT_CONFIG_MGR.team_resonance_config_mgr.getSatisfiedTeamResonances(elem_vec, total_promote_level);
//
//        // 删除出队角色的队伍共鸣
//        proto::TeamResonanceChangeNotify notify;
//        for (auto scene_team_avatar : diff_remove_avatar_vec)
//        {
//            Avatar avatar = scene_team_avatar.getAvatar();
//            if (avatar == null)
//            {
//                continue;
//            }
//            avatar.getTalentComp().clearTeamResonanceTalents(notify);
//        }
//
//        // 更新当前队伍中角色的队伍共鸣
//        foreachAvatar([team_resonance_id_vec, notify](Avatar  avatar)
//            {
//        // 试用角色不参与元素共鸣
//        if (avatar.getAvatarType() == proto::AVATAR_TYPE_TRIAL)
//        {
//            return FOREACH_CONTINUE;
//        }
//        avatar.getTalentComp().refreshTeamResonanceTalents(team_resonance_id_vec, notify);
//        return FOREACH_CONTINUE;
//    });
//
//        if (!notify.info_list().empty())
//        {
//            sendProto(notify);
//        }
    }

    // 刷一遍队伍共鸣（部分玩家进地城、角色突破、角色换技能库）
    void refreshCurTeamResonances()
    {
        refreshTeamResonances(List.of());
    }
}
