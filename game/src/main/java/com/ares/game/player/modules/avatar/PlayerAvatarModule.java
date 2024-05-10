package com.ares.game.player.modules.avatar;

import com.ares.core.excetion.UnknownLogicException;
import com.ares.game.player.Player;
import com.ares.game.player.PlayerModule;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneTeam;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.entity.avatarteam.AvatarTeamEntity;
import com.ares.game.scene.world.World;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PlayerAvatarModule extends PlayerModule {
    private static final Logger log = LoggerFactory.getLogger(PlayerAvatarModule.class);

    private Map<Long, Avatar> avatarMap = new HashMap<>();
    private Avatar curAvatar = null;
    private AvatarTeamEntity teamEntity = null;

    public PlayerAvatarModule(Player player) {
        super(ProtoCommon.GameModuleId.GMI_PlayerAvatar, player);
    }

    public int getAvatarCount() {
        return avatarMap.size();
    }

    public List<Avatar> getAvatarList() {
        return new ArrayList<>(avatarMap.values());
    }

    public boolean isAllAvatarDead() {
        return false;
    }

    public boolean isRevivableAfterAllDead() {
        return true;
    }

    public Avatar findAvatar(long guid) {
        return avatarMap.get(guid);
    }

    private void emplaceAvatar(Avatar avatar)
    {
        if (avatar == null)
        {
            throw new UnknownLogicException("avatar is null, uid:" + getPlayer().getUid());
        }

        if (avatar.getGuid() == 0)
        {
            throw new UnknownLogicException("avatar guid is 0, uid:" + getPlayer().getUid());
        }

        avatarMap.put(avatar.getGuid(), avatar);
    }

    private Avatar createAvatar(int avatarType, int configId, long avatarGuid)
    {
        if (avatarGuid == 0)
        {
//            avatarGuid = player_.getBasicComp().genGuid(GUID_AVATAR);
            avatarGuid = 10086;
        }
        if (avatarGuid == 0)
        {
            throw new UnknownLogicException("genGuid failed, avatarType:" + avatarType + " configId:" + configId + " uid:" + getPlayer().getUid());
        }
        Avatar avatar = new Avatar(avatarGuid, configId);
//        switch (avatarType)
//        {
//            case proto::AVATAR_TYPE_FORMAL:
//                avatar = MAKE_SHARED<FormalAvatar>(configId);
//                break;
//            case proto::AVATAR_TYPE_TRIAL:
//                avatar = MAKE_SHARED<TrialAvatar>(configId);
//                break;
//            case proto::AVATAR_TYPE_MIRROR:
//                avatar = MAKE_SHARED<MirrorAvatar>(configId);
//                break;
//            default:
//                LOG_ERROR + "unknown avatarType:" + avatarType + " player:" + player_;
//                break;
//        }


        avatar.setPlayer(player);
//        avatar.setBornTime(TimeUtils::getNow());

        return avatar;
    }

    public SceneTeam findSceneTeam()
    {
        Scene scene = player.getSceneModule().getCurScene();
        if (scene != null)
        {
            SceneTeam sceneTeam = scene.findSceneTeam();
            if (sceneTeam != null)
            {
                return sceneTeam;
            }
        }
        World world = player.getSceneModule().getCurWorld();
        if (world != null)
        {
            return world.getSceneTeam();
        }
        return null;
    }

    @Override
    public void fromBin(BinServer.PlayerDataBin bin) {
    }

    @Override
    public void toBin(BinServer.PlayerDataBin.Builder bin) {
    }

    @Override
    public void init() {
        Avatar avatar = createAvatar(0, 1, 0);
        avatar.setLifeState(ProtoCommon.LifeState.LIFE_ALIVE);
        emplaceAvatar(avatar);
        setCurAvatar(avatar);
    }
}
