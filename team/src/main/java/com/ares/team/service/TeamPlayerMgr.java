package com.ares.team.service;

import com.ares.core.utils.SnowFlake;
import com.ares.team.bean.Team;
import com.ares.team.player.TeamPlayer;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoTeam;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class TeamPlayerMgr {
    /**
     * key : teamId
     * value: team
     */
    private Map<Long, Team> teamMap = new HashMap<>();
    /**
     * key: userId
     * value teamPlayer
     */
    private final Map<Long, TeamPlayer> playerMap = new HashMap<>();

    public TeamPlayer getPlayer(long pid) {
        return playerMap.get(pid);
    }

    public TeamPlayer crateTeamPlayer(ProtoTeam.TeamMemberInfo teamMemberInfo, Team team) {
        return new TeamPlayer(teamMemberInfo, team);
    }

    public Team createTeam(ProtoInner.InnerCreateTeamReq createTeamReq, long ownerId) {
        Team team = new Team(3, ownerId);
        team.setId(SnowFlake.nextId());
        team.setName(createTeamReq.getTeamName());
        team.setDesc(createTeamReq.getTeamDes());
        addTeam(team.getId(), team);

        TeamPlayer teamPlayer = crateTeamPlayer(createTeamReq.getTeamMember(), team);
        team.addMember(teamPlayer);

        addTeamPlayer(teamPlayer);
        return team;
    }

    public void addTeam(long teamId, Team team) {
        teamMap.put(teamId, team);
    }

    public void deleteTeam(long teamId) {
        teamMap.remove(teamId);
    }

    public Team getTeam(long teamId) {
        return teamMap.get(teamId);
    }

    public void addTeamPlayer(TeamPlayer teamPlayer) {
        playerMap.put(teamPlayer.getUid(), teamPlayer);
    }

    public void deleteTeamPlayer(long uid) {
        playerMap.remove(uid);
    }

    public TeamPlayer getTeamPlayer(long uid) {
        return playerMap.get(uid);
    }

    public Collection<Team> getTeamList() {
        return teamMap.values();
    }
}
