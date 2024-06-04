package com.ares.game.scene.entity;

import com.ares.common.math.Vector3;
import com.ares.game.player.Player;
import com.ares.game.scene.Region;
import com.ares.game.scene.Scene;
import com.ares.game.scene.entity.avatarteam.AvatarTeamEntity;
import com.ares.game.scene.entity.eyepoint.PlayerEyePoint;
import com.ares.game.scene.entity.monster.Monster;
import com.ares.game.scene.entity.scene.SceneEntity;
import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;

public class EntityMgr {
    // 怪物参数
    @Getter
    @Setter
    public static class MonsterParam
    {
        int monsterId = 0;
        int configId = 0;
        int entityId = 0;
        Vector3 pos;
        Vector3 rot;
        int level = 1;         // 最终经过大世界调整后的等级
        int configLevel = 0;  // lua等配置的基础等级
        int reviseLevel = 0;  // 对基础等级的修正
    };


    // 创建怪物
    public static Monster createMonster(MonsterParam monsterParam)
    {
        int monsterId = monsterParam.monsterId;
//        MonsterExcelConfig* monster_config = GET_TXT_CONFIG_MGR.monster_config_mgr.findMonsterExcelConfig(monsterId);
//        if (monster_config == null)
//        {
//            LOG_WARNING + "findMonsterExcelConfig fails, monsterId:" + monsterId;
//            return null;
//        }

//        GroupPtr group = monsterParam.group;
//        // 怪物池创建怪物 要占用点位，防止点位被重复使用
//        if (group != null && monsterParam.monster_pool_id > 0)
//        {
//            if (group.isPointOccupied(monsterParam.config_id))
//            {
//                LOG_WARNING + " point config_id:" + monsterParam.config_id + " was occupied. @group: " + *group;
//                return null;
//            }
//        }

        Monster monster = new Monster(monsterId);
//#ifdef HK4E_DEBUG
        monster.setEntityName("Monster_" + monsterId);
//#endif

        monster.setEntityId(monsterParam.entityId);
        monster.initWithParam(monsterParam);
//
//        // 动态组件加载
//        if (monster_config.type == data::MONSTER_ENV_ANIMAL)
//        {
//            monster.addComp<MonsterEnvAnimalComp>();
//        }

//        if (group != null)
//        {
//            // fromBin应该在init之前
//            proto::GroupMonsterBin* monster_bin = group.findMonsterBin(monsterParam.config_id);
//            if (monster_bin != null && monster.fromBin(*monster_bin))
//            {
//                LOG_ERROR + "monster fromBin fails, config_id:" + monsterParam.config_id + *group;
//                return null;
//            }
//        }
//        // 添加到group
//        if (group != null && 0 != group.addEntity(*monster))
//        {
//            LOG_WARNING + "addEntity fails" + *group;
//            return null;
//        }
//        monster.setPreInstallSightGroup(monsterParam.sight_group);

        // init需要读取group相关数据
        monster.init();

//        // 怪物池创建怪物 要占用点位，防止点位被重复使用
//        if (group != null && monsterParam.monster_pool_id > 0)
//        {
//            group.occupyPoint(monsterParam.config_id);
//        }
//
//        // 处理global value, 需放在init之后
//        for (auto& [key, value] : monsterParam.global_value_map)
//        {
//            monster.setServerGlobalValue(key, value, false);
//        }

        return monster;
    }


    // 创建场景实体
    public static SceneEntity createSceneEntity(Scene scene) {
        SceneEntity entity = new SceneEntity();
        entity.setEntityId(EntityUtils.LEVEL_RUNTIMEID);
        entity.setEntityName("Scene_" + scene.getSceneId());
        entity.setScene(scene);
        return entity;
    }

    // 创建角色队伍实体
    public static AvatarTeamEntity createAvatarTeamEntity(Scene scene)
    {
        AvatarTeamEntity entity = new AvatarTeamEntity();
        int entityId = scene.genNewEntityId(ProtoScene.ProtEntityType.PROT_ENTITY_TEAM);
        entity.setEntityId(entityId);
        entity.setEntityName("AvatarTeam_" + entityId);
        entity.setScene(scene);;
//        if (0 != team_entity.initAbility())
//        {
//            LOG_WARNING + "initAbility fails" + *team_entity;
//            return null;
//        }
        return entity;
    }

    // 创建eye_point实体
    public static PlayerEyePoint createPlayerEyePoint(Player player, Scene scene, Region region, Region relatedBigRegion)
    {
        PlayerEyePoint eyePoint = new PlayerEyePoint();
        int entityId = scene.genNewEntityId(ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT);
        eyePoint.setEntityId(entityId);
        eyePoint.setPosition(region.getPosition());
        eyePoint.setPlayer(player);
        eyePoint.setRegion(region);
        eyePoint.setRelatedBigRegion(relatedBigRegion);
        return eyePoint;
    }
}
