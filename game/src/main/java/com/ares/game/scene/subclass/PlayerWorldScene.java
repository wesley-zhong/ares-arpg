package com.ares.game.scene.subclass;

import com.ares.common.math.Vector2;
import com.ares.common.math.Vector3;
import com.ares.game.player.Player;
import com.ares.game.scene.Scene;
import com.ares.game.scene.VisionContext;
import com.ares.game.scene.entity.EntityMgr;
import com.ares.game.scene.entity.monster.Monster;
import com.ares.game.scene.modules.sight.SceneGridSightModule;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;

public class PlayerWorldScene extends Scene {
    private SceneGridSightModule gridSightModule = new SceneGridSightModule(this);

    public PlayerWorldScene(int sceneId) {
        super(sceneId);
    }

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_WORLD;
    }

    @Override
    public World getOwnWorld() {
        return getOwnPlayerWorld();
    }

    @Override
    public PlayerWorld getOwnPlayerWorld() {
        Player owner = getOwnPlayer();
        if (owner == null)
        {
            return null;
        }
        return owner.getSceneModule().getMyPlayerWorld();
    }

    @Override
    public void init(Scene.SceneInitParam initParam) {
        super.init(initParam);

        getSightModule().init(Vector2.ZERO, new Vector2(10_0000, 10_0000));
        for (int i = 0; i < 100_000; i += 50) {
            int entityId = genNewEntityId(ProtoScene.ProtEntityType.PROT_ENTITY_MONSTER);
            EntityMgr.MonsterParam monsterParam = new EntityMgr.MonsterParam();
            monsterParam.setMonsterId(1001);
            monsterParam.setEntityId(entityId);
            monsterParam.setPos(new Vector3(i, i, i));
            monsterParam.setRot(Vector3.ZERO);
            Monster monster = EntityMgr.createMonster(monsterParam);
            monster.enterScene(this, VisionContext.MEET);
        }
    }
}
