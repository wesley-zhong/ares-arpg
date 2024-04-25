package com.ares.game.scene;

import com.ares.core.excetion.LogicException;
import com.ares.game.player.GamePlayer;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Scene {
    private static final Logger log = LoggerFactory.getLogger(Scene.class);

    public static String DEFAULT_SCENE_NAME = "default_scene";

    private SceneMgr sceneMgr;
    private int sceneId;
    private boolean defaultScene = false;
    private Map<Long, Long> playerEntityIdMap = new HashMap<>();
    private Map<Long, Integer> playerProfessionMap = new HashMap<>();
    private List<GamePlayer> players = new ArrayList<>();
    private Map<Long, EntityTRS> playerTrsMap = new HashMap<>();

    public int getSceneId() {
        return sceneId;
    }

    public boolean isDefaultScene() {
        return defaultScene;
    }

    public void init(SceneMgr sceneMgr, String scene_name, int scene_id, boolean reset_data, long create_actor_id){
        this.sceneMgr = sceneMgr;
        this.sceneId = scene_id;
        this.defaultScene = scene_name.equals(DEFAULT_SCENE_NAME);
    }

    public void update(long deltaMs) {
    }


    void initPlayerTRS(GamePlayer player)
    {
        Vector3 born_position = new Vector3(0,0,0);
        Vector3 born_rotation = new Vector3(0,0,0);
        Vector3 born_scale = new Vector3(0,0,0);

        EntityTRS trs = new EntityTRS();
        trs.setPosition(born_position);
        trs.setRotation(born_rotation);
        trs.setScale(born_scale);
        playerTrsMap.put(player.getUid(), trs);
    }

    void pbSyncTRS(long actor_id, ProtoScene.EntityTRS trs)
    {
        GamePlayer player = sceneMgr.getPlayer(actor_id);
        if (player == null)
            return;

        if (!playerTrsMap.containsKey(actor_id))
        {
            initPlayerTRS(player);
        }

        Vector3 position     = SceneUtil.pbUnreal2RecastPoint(trs.getPosition(), false);
        Vector3 rotation     = SceneUtil.pbUnreal2RecastPoint(trs.getRotation(), false);
        Vector3 scale        = SceneUtil.pbUnreal2RecastPoint(trs.getScale(), true);

        EntityTRS entityTRS = playerTrsMap.get(player.getUid());
        entityTRS.setPosition(position);
        entityTRS.setRotation(rotation);
        entityTRS.setScale(scale);
    }

    void announcePlayerEnterScene(long actorid)
    {
        // broadcast actor enter scene
        GamePlayer player = sceneMgr.getPlayer(actorid);
        if (player == null)
            return;

        int profession = playerProfessionMap.getOrDefault(actorid, 0);

        EntityTRS selfPlayerTrs = playerTrsMap.get(player.getUid());
        if (selfPlayerTrs != null) {
            ProtoScene.PlayerEnterSceneNtf.Builder self_message = ProtoScene.PlayerEnterSceneNtf.newBuilder();
            self_message.setUid(actorid);
            self_message.setProfession(profession);
            self_message.setTrs(ProtoScene.EntityTRS.newBuilder()
                            .setEntityID(actorid)
                            .setPosition(SceneUtil.recast2PbUnrealPoint(selfPlayerTrs.getPosition(), false))
                            .setRotation(SceneUtil.recast2PbUnrealPoint(selfPlayerTrs.getRotation(), false))
                            .setScale(SceneUtil.recast2PbUnrealPoint(selfPlayerTrs.getScale(), true))
                    .build());

            broadcast(ProtoCommon.MsgId.PLAYER_ENTER_SCENE_NTF, self_message.build(), 0);
        }

        // Tell this actor other actors enter scene
        for (GamePlayer player1 : players)
        {
            if (player1.getUid() != actorid)
            {
                EntityTRS player1Trs = playerTrsMap.get(player1.getUid());
                if (player1Trs != null) {
                    ProtoScene.PlayerEnterSceneNtf.Builder other_message = ProtoScene.PlayerEnterSceneNtf.newBuilder();
                    other_message.setUid(player1.getUid());
                    other_message.setProfession(playerProfessionMap.getOrDefault(player1.getUid(), 0));

                    other_message.setTrs(ProtoScene.EntityTRS.newBuilder()
                            .setEntityID(player1.getUid())
                            .setPosition(SceneUtil.recast2PbUnrealPoint(player1Trs.getPosition(), false))
                            .setRotation(SceneUtil.recast2PbUnrealPoint(player1Trs.getRotation(), false))
                            .setScale(SceneUtil.recast2PbUnrealPoint(player1Trs.getScale(), true))
                            .build());

                    sceneMgr.getPeerConn().sendGateWayMsg(actorid, ProtoCommon.MsgId.PLAYER_ENTER_SCENE_NTF_VALUE, other_message.build());
                }
            }
        }
    }

    void enterScene(long actor_id, int profession)
    {
        if (playerEntityIdMap.containsKey(actor_id))
        {
            return;
        }
        log.info("actor enter scene [actor_id:{}] [scene_id:{}]", actor_id, sceneId);

        GamePlayer player = sceneMgr.getPlayer(actor_id);
        if (player == null) {
            throw new LogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "player not found");
        }

        players.add(player);
        playerProfessionMap.put(actor_id, profession);
        playerEntityIdMap.put(actor_id, actor_id);
        if (!playerTrsMap.containsKey(actor_id)) {
            initPlayerTRS(player);
        }
        ProtoScene.ServerSceneFinishLoadingNtf finish_message = ProtoScene.ServerSceneFinishLoadingNtf.newBuilder().build();
        sceneMgr.getPeerConn().sendGateWayMsg(actor_id, ProtoCommon.MsgId.SERVER_SCENE_FINISH_LOADING_NTF_VALUE, finish_message);
        announcePlayerEnterScene(actor_id);
    }

    void announcePlayerLeaveScene(long actorid)
    {
        EntityTRS playerTrs = playerTrsMap.get(actorid);

        ProtoScene.PlayerLeaveSceneNtf.Builder message = ProtoScene.PlayerLeaveSceneNtf.newBuilder();
        message.setTrs(
            ProtoScene.EntityTRS.newBuilder().setEntityID(actorid)
                    .setRotation(SceneUtil.recast2PbUnrealPoint(playerTrs.getPosition(), false))
                    .setRotation(SceneUtil.recast2PbUnrealPoint(playerTrs.getRotation(), false))
                    .setScale(SceneUtil.recast2PbUnrealPoint(playerTrs.getScale(), true))
                    .build()
        );

        broadcast(ProtoCommon.MsgId.PLAYER_LEAVE_SCENE_NTF, message.build(), actorid);
    }

    void leaveScene(long actor_id)
    {
        if (playerEntityIdMap.containsKey(actor_id))
        {
            Iterator<GamePlayer> iterator = players.iterator();
            while (iterator.hasNext())
            {
                GamePlayer player = iterator.next();
                if (player.getUid() == actor_id)
                {
                    iterator.remove();
                    break;
                }
            }
            announcePlayerLeaveScene(actor_id);
            log.info("actor leave scene [actor_id:{}}] [scene_id:{}]", actor_id, sceneId);
            playerEntityIdMap.remove(actor_id);
            playerProfessionMap.remove(actor_id);
            playerTrsMap.remove(actor_id);
        }
    }

    void clientFinishLoading(long actor_id)
    {
    }

    void sendSceneMessage(long send_actor_id, ByteString content, boolean filter_self, int message_id)
    {
        ProtoScene.SyncSceneMessageNtf.Builder sync_message = ProtoScene.SyncSceneMessageNtf.newBuilder();
        sync_message.setMessageId(message_id);
        sync_message.setSceneMessage(ProtoScene.ClientMessagePackage.newBuilder().setContent(content).build());
        broadcast(ProtoCommon.MsgId.SYNC_SCENE_MESSAGE_NTF, sync_message.build(), filter_self ? send_actor_id : 0);
    }

    public void clientSyncMove(long actor_id, ProtoScene.ClientSyncMovePush push) {
        pbSyncTRS(actor_id, push.getTrs());
        Vector3 input_vector = SceneUtil.pbUnreal2RecastPoint(push.getTrs().getInputVector(), true);
        announcePlayerMove(actor_id, push.getTrs().getSpeed() / 100, input_vector, actor_id);
    }

    private void announcePlayerMove(long actorid, double speed, Vector3 input_vector, long filter_actor_id)
    {
        EntityTRS playerTrs = playerTrsMap.get(actorid);

        ProtoScene.PlayerMoveToNtf.Builder builder = ProtoScene.PlayerMoveToNtf.newBuilder();
        builder.setTrs(ProtoScene.EntityTRS.newBuilder()
                    .setEntityID(actorid)
                    .setRotation(SceneUtil.recast2PbUnrealPoint(playerTrs.getPosition(), false))
                    .setRotation(SceneUtil.recast2PbUnrealPoint(playerTrs.getRotation(), false))
                    .setScale(SceneUtil.recast2PbUnrealPoint(playerTrs.getScale(), true))
                    .setInputVector(SceneUtil.recast2PbUnrealPoint(input_vector, true))
                    .setSpeed(speed)
                .build());

        broadcast(ProtoCommon.MsgId.PLAYER_MOVE_TO_NTF, builder.build(), filter_actor_id);
    }

    boolean isEmpty(){
        return players.isEmpty();
    }

    void broadcast(ProtoCommon.MsgId msgId, Message message, long filterPlayerId) {
        broadcast(msgId, message, players, filterPlayerId);
    }

    void broadcast(ProtoCommon.MsgId msgId, Message message, List<GamePlayer> playerList, long filterPlayerId) {
        for (GamePlayer gamePlayer : playerList) {
            if (gamePlayer.getUid() == filterPlayerId)
                continue;

            sceneMgr.getPeerConn().sendGateWayMsg(gamePlayer.getUid(), msgId.getNumber(), message);
        }
    }
}
