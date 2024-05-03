package com.ares.team.player;

import com.ares.common.bean.Hero;
import com.ares.team.bean.Team;
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
    private Map<Long, Hero> heroes = new HashMap<>(8);

    public TeamPlayer(long uid, String userName, Team team) {
        this.uid = uid;
        this.name = userName;
        this.team = team;
    }

    public void addHero(Hero hero) {
        heroes.put(hero.getId(), hero);
    }

    public Hero getHero(long heroId) {
        return heroes.get(heroId);
    }
}
