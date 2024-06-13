package com.ares.gateway.service;

import com.ares.common.bean.ServerType;
import com.ares.common.util.LRUCache;
import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.dal.game.UserOnlineService;
import com.ares.dal.game.UserOnlineStateDO;
import com.ares.discovery.DiscoveryService;
import com.ares.gateway.bean.PlayerSession;
import com.ares.gateway.discovery.OnDiscoveryWatchService;
import com.ares.gateway.network.PeerConn;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.bean.TcpConnServerInfo;
import com.game.protoGen.*;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.atomic.AtomicLong;

/**
 * this Obj will be call in multi-thread  env
 * the thread will be hashed  by uid
 * the same uid will be called in the same thread
 */
@Component
@Slf4j
@NotThreadSafe
public class SessionServiceImp implements SessionService, InitializingBean {
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private DiscoveryService discoveryService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatchService;
    @Autowired
    private UserOnlineService userOnlineService;
    @Value("${server.max-player-count:20000}")
    private int maxPlayerCount;

    private AtomicLong sidGen = new AtomicLong(System.currentTimeMillis());

    private LRUCache<Long, PlayerSession> playerSessionMap;


    @Override
    public void gameLogin(AresTKcpContext aresTKcpContext, ProtoGame.GameLoginPush loginRequest) {
        /****
         * do something
         */
        PlayerSession playerSession = new PlayerSession(loginRequest.getUid(), aresTKcpContext);
        long sessionId = sidGen.incrementAndGet();
        playerSession.setSid(sessionId);
        playerSession.cacheMySelf();
        log.info("----------------  game loginReq={} new session={}", loginRequest, playerSession);
        // save user current session   and close old connection
        PlayerSession existPlayerSession = playerSessionMap.put(loginRequest.getUid(), playerSession);
        if (existPlayerSession != null) {
            log.info("XXXX player session:{} already exist should be closed", existPlayerSession);
            existPlayerSession.close();
        }
        selectGameServerLogin(playerSession, loginRequest);
    }


    private void selectGameServerLogin(PlayerSession playerSession, ProtoGame.GameLoginPush loginRequest) {
        //get user last login game server
        UserOnlineStateDO userPreSetCurOnlineStatus = createCurUserOnlineStateDO();
        UserOnlineStateDO userOnlineStateDO = userOnlineService.setUserOnlineStatus(loginRequest.getUid(), userPreSetCurOnlineStatus);
        //继续选择玩家的 gameServer
        String gameSrvId = userOnlineStateDO.getGmSrId();
        TcpConnServerInfo tcpConnServerInfo = peerConn.getServerConnByServiceId(gameSrvId);
        //如果gameServer connection 不存在 ，需要重新选择gameServer， 并更新
        if (tcpConnServerInfo == null) {
            userOnlineStateDO.setGmSrId(userPreSetCurOnlineStatus.getGmSrId());
            //被别的账号给修改了
            userOnlineStateDO = userOnlineService.setUserOnlineStatus(loginRequest.getUid(), userOnlineStateDO);
            //更新失败被其他进程修改掉了，因为不确定其他进程 是否已经发了 踢出出玩家的消息，通知客户端重新登录，重新走流程
            if (userOnlineStateDO.getGmSrId() != null && !userOnlineStateDO.getGmSrId().equals(userPreSetCurOnlineStatus.getGmSrId())) {
                ProtoCommon.MsgHeader msgHeader = ProtoCommon.MsgHeader.newBuilder()
                        .setMsgId(ProtoMsgId.MsgId.GAME_LOGIN_NTF_VALUE)
                        .setErrCode(ProtoErrorCode.ErrCode.FAILED_VALUE).build();
                playerSession.getAresTKcpContext().send(AresPacket.create(msgHeader, null));
                playerSession.getAresTKcpContext().close();
                log.error("uid ={} 多个账号在同时登录失败，重新登录", loginRequest.getUid());
                return;
            }
            tcpConnServerInfo = peerConn.getServerConnByServiceId(userOnlineStateDO.getGmSrId());
        }
        Channel channel = tcpConnServerInfo.hashChannel(loginRequest.getUid());
        if (channel == null) {
            log.error(" uid ={} gameSrvId ={} not found connection", loginRequest.getUid(), tcpConnServerInfo.getServerNodeInfo().getServiceId());
            return;
        }
        ProtoInner.InnerGameLoginRequest innerLoginRequest = ProtoInner.InnerGameLoginRequest.newBuilder()
                .setSid(playerSession.getSid())
                .setTargetId(userOnlineStateDO.getTargetId())
                .build();
        peerConn.send(channel, loginRequest.getUid(), ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE, innerLoginRequest);
        peerConn.recordPlayerPeerChannel(loginRequest.getUid(), channel);

        //设置超时 only for example 其实不用，用心跳包即可
//        if (playerSession.getAresTimerTask() != null) {
//            playerSession.getAresTimerTask().cancel();
//        }
//        AresTimerTask<?> aresTimerTask = ScheduleService.INSTANCE.executeTimerTaskWithMS(loginRequest.getUid(), this::onPlayerLonginGamsrvTimeOut, playerSession, 10000);
//        playerSession.setAresTimerTask(aresTimerTask);
    }

    private void onPlayerLonginGamsrvTimeOut(PlayerSession playerSession) {
        log.error("player id ={} login timeout ,should be close session,", playerSession.getUid());
        playerSession.close();
    }

    private UserOnlineStateDO createCurUserOnlineStateDO() {
        ServerNodeInfo lowerLoadServerNodeInfo = onDiscoveryWatchService.getLowerLoadServerNodeInfo(ServerType.GAME.getValue());
        if (lowerLoadServerNodeInfo == null) {
            log.error("not game server exist");
            return null;
        }
        UserOnlineStateDO userOnlineStateDO = new UserOnlineStateDO();
        userOnlineStateDO.setGmSrId(lowerLoadServerNodeInfo.getServiceId());
        return userOnlineStateDO;
    }


    @Override
    public void gameSrvLoginResponse(ProtoInner.InnerGameLoginResponse response) {
        PlayerSession playerSession = playerSessionMap.get(response.getUid());
        if (playerSession == null) {
            log.error("uid = {} not login in gateway", response.getUid());
            return;
        }
        if (response.getSid() != playerSession.getSid()) {
            log.warn("uid = {} sid={} have re-login", response.getUid(), response.getSid());
            return;
        }

        if (response.getErrCode() != 0) {
            log.error(" playerId = {} sid={} login failed errCode ={}  ", response.getUid(), response.getSid(), response.getErrCode());
            sendPlayerErrMsg(playerSession, ProtoMsgId.MsgId.GAME_LOGIN_NTF_VALUE, response.getErrCode());
            closeAndRemovePlayerSession(playerSession);
            return;
        }

        playerSession.setValid();
        log.info(" playerId = {} sid ={} login success  ", response.getUid(), response.getSid());
        ProtoGame.GameLoginNtf gameLoginRes = ProtoGame.GameLoginNtf.newBuilder()
                .setUid(playerSession.getUid())
                .setServerTime(System.currentTimeMillis()).build();
        sendPlayerMsg(playerSession, ProtoMsgId.MsgId.GAME_LOGIN_NTF_VALUE, gameLoginRes);

        //update online count to etcd
        updateMyNodeInfo();
    }

    @Override
    public void kickOutPlayer(long uid, long sid) {
        PlayerSession playerSession = playerSessionMap.get(uid);
        if (playerSession == null || playerSession.getSid() != sid) {
            return;
        }
        log.info("kick-out player ={}", uid);
        playerSession.close();
        playerSessionMap.remove(playerSession.getUid());
        peerConn.removePlayerConn(playerSession.getUid());
    }

    @Override
    public void playerDisconnect(PlayerSession playerSession) {
        log.info("-------------  player disconnect ={}", playerSession);
        playerSessionMap.remove(playerSession.getUid());
        peerConn.removePlayerConn(playerSession.getUid());
        //update online count to etcd
        updateMyNodeInfo();
    }

    @Override
    public PlayerSession getPlayerSession(long uid) {
        return playerSessionMap.get(uid);
    }

    @Override
    public void playerChangeScene(long uid, ProtoInner.InnerSceneChangeReq innerSceneChangeReq) {
        PlayerSession playerSession = playerSessionMap.get(uid);
        if (playerSession == null) {
            log.error("pid = {} not found", uid);
            return;
        }
        TcpConnServerInfo tcpConnServerInfo = peerConn.getServerConnByServiceId(innerSceneChangeReq.getGameSrvId());
        if (tcpConnServerInfo == null) {
            log.error("serviceId  = {} not found", innerSceneChangeReq.getGameSrvId());
            return;
        }

        UserOnlineStateDO userOnlineStateDO = userOnlineService.getUserOnlineStateDO(uid);
        userOnlineStateDO.setTargetId(innerSceneChangeReq.getTargetId());
        userOnlineStateDO.setGmSrId(innerSceneChangeReq.getGameSrvId());
        UserOnlineStateDO ret = userOnlineService.setUserOnlineStatus(uid, userOnlineStateDO);
        if (!ret.getGmSrId().equals(innerSceneChangeReq.getGameSrvId()) || ret.getTargetId() != innerSceneChangeReq.getTargetId()) {
            log.error("XXXXXXXXXXXXXXXXXXXX  playerChangeScene  req={}  changed failed", innerSceneChangeReq);
            return;
        }

        Channel channel = tcpConnServerInfo.hashChannel(uid);
        ProtoInner.InnerGameLoginRequest innerLoginRequest = ProtoInner.InnerGameLoginRequest.newBuilder()
                .setTargetId(innerSceneChangeReq.getTargetId())
                .setSid(playerSession.getSid()).build();

        peerConn.send(channel, uid, ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE, innerLoginRequest);
        peerConn.recordPlayerPeerChannel(uid, channel);
        log.info("############# uid ={} changed to gameServer={} targetId ={}", uid, tcpConnServerInfo.getServerNodeInfo(), innerSceneChangeReq.getTargetId());
    }

    @Override
    public void sendPlayerMsg(long uid, int msgId, Message body) {
        PlayerSession playerSession = playerSessionMap.get(uid);
        if (playerSession == null) {
            log.error("uid = {}   msgId ={} not login in gateway", uid, msgId);
            return;
        }
        AresPacket aresPacket = AresPacket.create(msgId, body);
        playerSession.getAresTKcpContext().send(aresPacket);
    }

    @Override
    public void sendPlayerErrMsg(PlayerSession playerSession, int msgId, int errCode) {
        ProtoCommon.MsgHeader msgHeader = ProtoCommon.MsgHeader.newBuilder()
                .setErrCode(errCode)
                .setUid(playerSession.getUid())
                .setMsgId(msgId)
                .build();
        AresPacket aresPacket = AresPacket.create(msgHeader, null);
        playerSession.getAresTKcpContext().send(aresPacket);
    }

    @Override
    public void sendPlayerMsg(PlayerSession playerSession, int msgId, Message body) {
        AresPacket aresPacket = AresPacket.create(msgId, body);
        playerSession.getAresTKcpContext().send(aresPacket);
    }

    @Override
    public void sendPlayerMsg(long uid, AresPacket aresPacket) {
        PlayerSession playerSession = playerSessionMap.get(uid);
        if (playerSession == null) {
            log.error(" uid = {}   not login in gateway", uid);
            return;
        }
        playerSession.getAresTKcpContext().send(aresPacket);
    }

    @Override
    public void sendPlayerMsg(long uid, ByteBuf body) {
        PlayerSession playerSession = playerSessionMap.get(uid);
        if (playerSession == null) {
            log.error("uid = {}  not login in gateway", uid);
            return;
        }
        playerSession.getAresTKcpContext().send(body);
    }

    private void closeAndRemovePlayerSession(PlayerSession playerSession) {
        playerSessionMap.remove(playerSession.getUid());
        playerSession.close();
    }


    private boolean checkPlayerToken(long uid, String token) {
//        try {
//            GetResponse getResponse = discoveryService.getEtcdClient().getKVClient()
//                    .get(ByteSequence.from(uid + "", StandardCharsets.UTF_8)).get();
//            if(getResponse.getCount() != 1){
//                return false;
//            }
//            KeyValue keyValue = getResponse.getKvs().get(0);
//            return  keyValue.getValue().toString(StandardCharsets.UTF_8).equals(token);
//        } catch (Exception e) {
//            log.error("-----error", e);
//        }
        return true;

    }

    private void updateMyNodeInfo() {
        ServerNodeInfo myNodeInfo = discoveryService.getEtcdRegister().getMyselfNodeInfo();
        int online = playerSessionMap.size();
        myNodeInfo.setOnlineCount(online);
        discoveryService.getEtcdRegister().updateServerNodeInfo(myNodeInfo);
    }

    @Override
    public void afterPropertiesSet() {
        playerSessionMap = new LRUCache<>(maxPlayerCount);
    }
}
