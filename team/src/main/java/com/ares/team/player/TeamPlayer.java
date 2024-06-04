package com.ares.team.player;

import com.ares.common.bean.Hero;
import com.ares.team.bean.Team;
import com.game.protoGen.ProtoTeam;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TeamPlayer {
    private final long uid;
    private final String name;
    private final Team team;
    private int level;
    private final String fromGsrId;
    private Map<Long, Hero> heroes = new HashMap<>(8);

    public TeamPlayer(ProtoTeam.TeamMemberInfo teamMemberInfo,  Team team){
        this.uid = teamMemberInfo.getActorId();
        this.name = teamMemberInfo.getNickName();
        this.team = team;
        this.fromGsrId = teamMemberInfo.getFromGsrId();
    }

    public void addHero(Hero hero) {
        heroes.put(hero.getId(), hero);
    }

    public Hero getHero(long heroId) {
        return heroes.get(heroId);
    }
}
