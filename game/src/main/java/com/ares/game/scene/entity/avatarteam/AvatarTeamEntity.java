package com.ares.game.scene.entity.avatarteam;

import com.ares.game.player.Player;
import com.ares.game.scene.Scene;
import com.ares.game.scene.VisionContext;
import com.ares.game.scene.entity.creature.Creature;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarTeamEntity extends Creature {
    private Player player;

    @Override
    public ProtoScene.ProtEntityType getEntityType() {
        return ProtoScene.ProtEntityType.PROT_ENTITY_TEAM;
    }

    @Override
    public void enterScene(Scene scene, VisionContext context) {
    }
}
