package com.ares.game.scene;

import com.ares.core.excetion.LogicException;
import com.ares.game.network.PeerConn;
import com.ares.game.player.Player;
import com.ares.game.service.PlayerRoleService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoOldScene;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class OldSceneMgr implements InitializingBean {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;

    private int max_scene_id;
    private int default_scene_id;
    Map<Long, Integer> actor_scene_map = new HashMap<>();
    Map<Integer, OldScene> scene_map = new HashMap<>();
    private List<SceneInfo> scene_list = new ArrayList<>();

    private void init(){
        createScene(OldScene.DEFAULT_SCENE_NAME, 0, false, null);
    }

    private void update() {

    }

    OldScene getSceneBySceneID(int scene_id)
    {
        return scene_map.get(scene_id);
    }

    OldScene getSceneByActorID(long actor_id)
    {
        return getSceneBySceneID(actor_scene_map.get(actor_id));
    }

    public void createScene(String scene_name, long create_actor_id, boolean reset_data, ProtoOldScene.OldCreateSceneRes.Builder message)
    {
        if (actor_scene_map.containsKey(create_actor_id))
        {
            throw new LogicException(ProtoCommon.ErrCode.ERR_ALREADY_IN_SCENE_VALUE, "");
        }
        max_scene_id             = max_scene_id + 1;
        int scene_id            = max_scene_id;
        OldScene scene              = new OldScene();
        scene_map.put(scene_id,   scene);
        SceneInfo info = new SceneInfo();
        info.scene_id     = scene_id;
        info.scene_name   = scene_name;
        info.player_count = 0;
        scene_list.add(info);
        scene.init(this, scene_name, scene_id, reset_data, create_actor_id);
        if (scene.isDefaultScene())
        {
            default_scene_id = scene_id;
        }
        if (message != null) {
            message.setSceneId(scene_id);
        }

        ProtoOldScene.OldCreateSceneFinishNtf.Builder builder = ProtoOldScene.OldCreateSceneFinishNtf.newBuilder();
        builder.setSceneId(scene_id);
        getPeerConn().sendGateWayMsg(create_actor_id, ProtoCommon.MsgId.OLD_CREATE_SCENE_FINISH_NTF_VALUE, builder.build());
    }

    public List<SceneInfo> getSceneList()
    {
        return scene_list;
    }

    public void enterScene(int scene_id, long actorid, int profession)
    {
        if (actor_scene_map.containsKey(actorid))
        {
            throw new LogicException(ProtoCommon.ErrCode.ERR_ALREADY_IN_SCENE_VALUE, "");
        }
        OldScene scene = getSceneBySceneID(scene_id);
        if (scene == null)
        {
            scene = getSceneBySceneID(default_scene_id);
            if (scene == null)
            {
                throw new LogicException(ProtoCommon.ErrCode.ERR_NOT_EXIST_SCENE_VALUE, "");
            }
        }

        scene.enterScene(actorid, profession);
        actor_scene_map.put(actorid, scene_id);

        for (SceneInfo sceneInfo : scene_list)
        {
            if (sceneInfo.scene_id == scene_id)
            {
                ++sceneInfo.player_count;
                break;
            }
        }
    }

    public void enterDefaultScene(long actorid, int profession)
    {
        enterScene(default_scene_id, actorid, profession);
        OldScene scene = getSceneByActorID(actorid);
        if (scene == null){
            throw new LogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "");
        }
        Player player = getPlayer(actorid);
        if (player == null){
            throw new LogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "");
        }
    }

    public void leaveScene(long actorid)
    {
        int scene_id = actor_scene_map.get(actorid);
        if (scene_id == 0) {
            throw new LogicException(ProtoCommon.ErrCode.ERR_NOT_IN_SCENE_VALUE, "");
        }
        OldScene scene = getSceneBySceneID(scene_id);
        if (scene == null) {
            throw new LogicException(ProtoCommon.ErrCode.ERR_NOT_EXIST_SCENE_VALUE, "");
        }
        scene.leaveScene(actorid);
        for (SceneInfo sceneInfo : scene_list)
        {
            if (sceneInfo.scene_id == scene_id)
            {
                ++sceneInfo.player_count;
                break;
            }
        }
        actor_scene_map.remove(actorid);
    }

    public void clientFinishLoading(long actorid)
    {
        OldScene scene = getSceneByActorID(actorid);
        if (scene == null) {
            throw new LogicException(ProtoCommon.ErrCode.ERR_NOT_IN_SCENE_VALUE, "");
        }
        scene.clientFinishLoading(actorid);
    }

    public void sendSceneMessage(long actorid, ByteString content, boolean filter_self, int message_id)
    {
        OldScene scene = getSceneByActorID(actorid);
        if (scene == null) {
            throw new LogicException(ProtoCommon.ErrCode.ERR_NOT_IN_SCENE_VALUE, "");
        }
        scene.sendSceneMessage(actorid, content, filter_self, message_id);
    }

    public void removeScene(int scene_id)
    {
        OldScene scene = getSceneBySceneID(scene_id);
        if (scene == null) {
            throw new LogicException(ProtoCommon.ErrCode.ERR_NOT_EXIST_SCENE_VALUE, "");
        }

        if (!scene.isEmpty())
        {
            log.error("Scene[{}] not empty but remove scene", scene_id);
        }

        scene_map.remove(scene_id);

        for (Iterator<SceneInfo> iter = scene_list.iterator(); iter.hasNext();)
        {
            SceneInfo sceneInfo = iter.next();
            if (sceneInfo.scene_id == scene_id)
            {
                iter.remove();
                break;
            }
        }
    }

    public void clientSyncMove(long actor_id, ProtoOldScene.OldClientSyncMovePush push)
    {
        OldScene scene = getSceneByActorID(actor_id);
        if (scene == null) return;

        scene.clientSyncMove(actor_id, push);
    }

    public Player getPlayer(long uid) {
        return playerRoleService.getPlayer(uid);
    }

    public PeerConn getPeerConn() {
        return peerConn;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public static class SceneInfo {
        String scene_name;
        int scene_id;
        int  player_count;

        public String getScene_name() {
            return scene_name;
        }

        public int getScene_id() {
            return scene_id;
        }

        public int getPlayer_count() {
            return player_count;
        }
    };
}
