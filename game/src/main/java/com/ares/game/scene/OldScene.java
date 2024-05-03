package com.ares.game.scene;

import com.ares.common.math.Vector3;
import com.ares.core.excetion.LogicException;
import com.ares.game.player.Player;
import com.ares.game.scene.entity.EntityTRS;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoOldScene;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OldScene {
    private static final Logger log = LoggerFactory.getLogger(OldScene.class);

    public static String DEFAULT_SCENE_NAME = "default_scene";

    private OldSceneMgr sceneMgr;
    private int sceneId;
    private boolean defaultScene = false;
    private Map<Long, Long> playerEntityIdMap = new HashMap<>();
    private Map<Long, Integer> playerProfessionMap = new HashMap<>();
    private List<Player> players = new ArrayList<>();
    private Map<Long, EntityTRS> playerTrsMap = new HashMap<>();

    public int getSceneId() {
        return sceneId;
    }

    public boolean isDefaultScene() {
        return defaultScene;
    }

    public void init(OldSceneMgr sceneMgr, String scene_name, int scene_id, boolean reset_data, long create_actor_id){
        this.sceneMgr = sceneMgr;
        this.sceneId = scene_id;
        this.defaultScene = scene_name.equals(DEFAULT_SCENE_NAME);
    }

    public void update(long deltaMs) {
    }


    void initPlayerTRS(Player player)
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

    void pbSyncTRS(long actor_id, ProtoOldScene.EntityTRS trs)
    {
        Player player = sceneMgr.getPlayer(actor_id);
        if (player == null)
            return;

        if (!playerTrsMap.containsKey(actor_id))
        {
            initPlayerTRS(player);
        }

        Vector3 position     = OldSceneUtil.pbUnreal2RecastPoint(trs.getPosition(), false);
        Vector3 rotation     = OldSceneUtil.pbUnreal2RecastPoint(trs.getRotation(), false);
        Vector3 scale        = OldSceneUtil.pbUnreal2RecastPoint(trs.getScale(), true);

        EntityTRS entityTRS = playerTrsMap.get(player.getUid());
        entityTRS.setPosition(position);
        entityTRS.setRotation(rotation);
        entityTRS.setScale(scale);
    }

    void announcePlayerEnterScene(long actorid)
    {
        // broadcast actor enter scene
        Player player = sceneMgr.getPlayer(actorid);
        if (player == null)
            return;

        int profession = playerProfessionMap.getOrDefault(actorid, 0);

        EntityTRS selfPlayerTrs = playerTrsMap.get(player.getUid());
        if (selfPlayerTrs != null) {
            ProtoOldScene.OldPlayerEnterSceneNtf.Builder self_message = ProtoOldScene.OldPlayerEnterSceneNtf.newBuilder();
            self_message.setUid(actorid);
            self_message.setProfession(profession);
            self_message.setTrs(ProtoOldScene.EntityTRS.newBuilder()
                            .setEntityID(actorid)
                            .setPosition(OldSceneUtil.recast2PbUnrealPoint(selfPlayerTrs.getPosition(), false))
                            .setRotation(OldSceneUtil.recast2PbUnrealPoint(selfPlayerTrs.getRotation(), false))
                            .setScale(OldSceneUtil.recast2PbUnrealPoint(selfPlayerTrs.getScale(), true))
                    .build());

            broadcast(ProtoCommon.MsgId.OLD_PLAYER_ENTER_SCENE_NTF, self_message.build(), 0);
        }

        // Tell this actor other actors enter scene
        for (Player player1 : players)
        {
            if (player1.getUid() != actorid)
            {
                EntityTRS player1Trs = playerTrsMap.get(player1.getUid());
                if (player1Trs != null) {
                    ProtoOldScene.OldPlayerEnterSceneNtf.Builder other_message = ProtoOldScene.OldPlayerEnterSceneNtf.newBuilder();
                    other_message.setUid(player1.getUid());
                    other_message.setProfession(playerProfessionMap.getOrDefault(player1.getUid(), 0));

                    other_message.setTrs(ProtoOldScene.EntityTRS.newBuilder()
                            .setEntityID(player1.getUid())
                            .setPosition(OldSceneUtil.recast2PbUnrealPoint(player1Trs.getPosition(), false))
                            .setRotation(OldSceneUtil.recast2PbUnrealPoint(player1Trs.getRotation(), false))
                            .setScale(OldSceneUtil.recast2PbUnrealPoint(player1Trs.getScale(), true))
                            .build());

                    sceneMgr.getPeerConn().sendGateWayMsg(actorid, ProtoCommon.MsgId.OLD_PLAYER_ENTER_SCENE_NTF_VALUE, other_message.build());
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

        Player player = sceneMgr.getPlayer(actor_id);
        if (player == null) {
            throw new LogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "player not found");
        }

        players.add(player);
        playerProfessionMap.put(actor_id, profession);
        playerEntityIdMap.put(actor_id, actor_id);
        if (!playerTrsMap.containsKey(actor_id)) {
            initPlayerTRS(player);
        }
        ProtoOldScene.OldServerSceneFinishLoadingNtf finish_message = ProtoOldScene.OldServerSceneFinishLoadingNtf.newBuilder().build();
        sceneMgr.getPeerConn().sendGateWayMsg(actor_id, ProtoCommon.MsgId.OLD_SERVER_SCENE_FINISH_LOADING_NTF_VALUE, finish_message);
        announcePlayerEnterScene(actor_id);
    }

    void announcePlayerLeaveScene(long actorid)
    {
        EntityTRS playerTrs = playerTrsMap.get(actorid);

        ProtoOldScene.OldPlayerLeaveSceneNtf.Builder message = ProtoOldScene.OldPlayerLeaveSceneNtf.newBuilder();
        message.setTrs(
            ProtoOldScene.EntityTRS.newBuilder().setEntityID(actorid)
                    .setRotation(OldSceneUtil.recast2PbUnrealPoint(playerTrs.getPosition(), false))
                    .setRotation(OldSceneUtil.recast2PbUnrealPoint(playerTrs.getRotation(), false))
                    .setScale(OldSceneUtil.recast2PbUnrealPoint(playerTrs.getScale(), true))
                    .build()
        );

        broadcast(ProtoCommon.MsgId.OLD_PLAYER_LEAVE_SCENE_NTF, message.build(), actorid);
    }

    void leaveScene(long actor_id)
    {
        if (playerEntityIdMap.containsKey(actor_id))
        {
            Iterator<Player> iterator = players.iterator();
            while (iterator.hasNext())
            {
                Player player = iterator.next();
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
        ProtoOldScene.OldSyncSceneMessageNtf.Builder sync_message = ProtoOldScene.OldSyncSceneMessageNtf.newBuilder();
        sync_message.setMessageId(message_id);
        sync_message.setSceneMessage(ProtoOldScene.ClientMessagePackage.newBuilder().setContent(content).build());
        broadcast(ProtoCommon.MsgId.OLD_SYNC_SCENE_MESSAGE_NTF, sync_message.build(), filter_self ? send_actor_id : 0);
    }

    public void clientSyncMove(long actor_id, ProtoOldScene.OldClientSyncMovePush push) {
        pbSyncTRS(actor_id, push.getTrs());
        Vector3 input_vector = OldSceneUtil.pbUnreal2RecastPoint(push.getTrs().getInputVector(), true);
        announcePlayerMove(actor_id, push.getTrs().getSpeed() / 100, input_vector, actor_id);
    }

    private void announcePlayerMove(long actorid, double speed, Vector3 input_vector, long filter_actor_id)
    {
        EntityTRS playerTrs = playerTrsMap.get(actorid);

        ProtoOldScene.OldPlayerMoveToNtf.Builder builder = ProtoOldScene.OldPlayerMoveToNtf.newBuilder();
        builder.setTrs(ProtoOldScene.EntityTRS.newBuilder()
                    .setEntityID(actorid)
                    .setRotation(OldSceneUtil.recast2PbUnrealPoint(playerTrs.getPosition(), false))
                    .setRotation(OldSceneUtil.recast2PbUnrealPoint(playerTrs.getRotation(), false))
                    .setScale(OldSceneUtil.recast2PbUnrealPoint(playerTrs.getScale(), true))
                    .setInputVector(OldSceneUtil.recast2PbUnrealPoint(input_vector, true))
                    .setSpeed(speed)
                .build());

        broadcast(ProtoCommon.MsgId.OLD_PLAYER_MOVE_TO_NTF, builder.build(), filter_actor_id);
    }

    boolean isEmpty(){
        return players.isEmpty();
    }

    void broadcast(ProtoCommon.MsgId msgId, Message message, long filterPlayerId) {
        broadcast(msgId, message, players, filterPlayerId);
    }

    void broadcast(ProtoCommon.MsgId msgId, Message message, List<Player> playerList, long filterPlayerId) {
        for (Player player : playerList) {
            if (player.getUid() == filterPlayerId)
                continue;

            sceneMgr.getPeerConn().sendGateWayMsg(player.getUid(), msgId.getNumber(), message);
        }
    }
}
