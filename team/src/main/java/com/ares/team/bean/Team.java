package com.ares.team.bean;

import com.ares.team.enums.TeamStateEnum;
import com.ares.team.player.TeamPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Team {
    private long id;
    private long ownerId;
    private String name;
    private String desc;
    private int MAX_COUNT;
    private TeamStateEnum state;
    private final Map<Long, TeamPlayer> teamMembers;

    public Team(int memberCount, long ownerId) {
        this.ownerId = ownerId;
        teamMembers = new HashMap<>(memberCount);
        MAX_COUNT = memberCount;
        state = TeamStateEnum.CREATED;
    }

    public void start() {
        state = TeamStateEnum.STARTED;
    }

    public TeamPlayer getOwner() {
        return teamMembers.get(ownerId);
    }

    public void addMember(TeamPlayer teamPlayer) {
        teamMembers.put(teamPlayer.getUid(), teamPlayer);
    }

    public void deleteMember(TeamPlayer teamPlayer) {
        teamMembers.remove(teamPlayer.getUid());
        if (this.ownerId == teamPlayer.getUid()) {
            changeOwner();
        }
    }

    private void changeOwner() {
        if (teamMembers.isEmpty()) {
            return;
        }
        TeamPlayer next = teamMembers.values().iterator().next();
        this.ownerId = next.getUid();
    }

    public TeamPlayer getMember(long uid) {
        return teamMembers.get(uid);
    }

    public int getCurSize() {
        return teamMembers.size();
    }

    public int getMaxNum() {
        return MAX_COUNT;
    }

    public boolean isMax() {
        return teamMembers.size() == MAX_COUNT;
    }

    public boolean isEmpty() {
        return teamMembers.isEmpty();
    }
}
