package com.ares.gateway.service;

import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.discovery.DiscoveryService;
import com.ares.gateway.bean.PlayerSession;
import com.ares.gateway.network.PeerConn;
import com.ares.transport.bean.ServerNodeInfo;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionServiceImp implements SessionService {
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private DiscoveryService discoveryService;

    private final Map<Long, AresTKcpContext> playerChannelContext = new ConcurrentHashMap<>();


    @Override
    public void gameLogin(AresTKcpContext aresTKcpContext, ProtoGame.GameLoginReq loginRequest) {
        log.info("----------------  game loginReq={}", loginRequest);
        /****
         * do something
         //         */;
        aresTKcpContext.clearPackageData();
        AresTKcpContext existContext = playerChannelContext.put(loginRequest.getUid(), aresTKcpContext);
        //close old connection
        if (existContext != null) {
            existContext.close();
        }
        //save player conn
        playerChannelContext.put(loginRequest.getUid(), aresTKcpContext);

        ProtoInner.InnerGameLoginRequest innerLoginRequest = ProtoInner.InnerGameLoginRequest.newBuilder()
                .setUid(loginRequest.getUid())
                .setToken(loginRequest.getGameToken()).build();

        peerConn.sendToGameMsg(loginRequest.getUid(), ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_REQ_VALUE, innerLoginRequest);
    }


    @Override
    public void gameLoginSuccess(ProtoInner.InnerGameLoginResponse response) {
        PlayerSession playerSession = new PlayerSession(response.getUid());
        AresTKcpContext channelHandlerContext = playerChannelContext.get(response.getUid());
        if (channelHandlerContext == null) {
            log.error("uid = {}    not login in gateway", response.getUid());
            return;
        }
        channelHandlerContext.cacheObj(playerSession);
        //update online count to etcd
        updateMyNodeInfo();
    }

    @Override
    public void playerDisconnect(PlayerSession playerSession) {
        log.info("-------------  player disconnect ={}", playerSession);
        playerChannelContext.remove(playerSession.getUid());

        //update online count to etcd
        updateMyNodeInfo();
    }

    @Override
    public void sendPlayerMsg(long uid, int msgId, Message body) {
        AresTKcpContext channelHandlerContext = playerChannelContext.get(uid);
        if (channelHandlerContext == null) {
            log.error("uid = {}   msgId ={} not login in gateway", uid, msgId);
            return;
        }
        AresPacket aresPacket = AresPacket.create(msgId, body);
        channelHandlerContext.send(aresPacket);
    }

    @Override
    public void sendPlayerMsg(long uid, AresPacket aresPacket) {
        AresTKcpContext aresTKcpContext = playerChannelContext.get(uid);
        if (aresTKcpContext == null) {
            log.error(" uid = {}   msgId ={} not login in gateway", uid, aresPacket.getMsgId());
            return;
        }
        aresTKcpContext.send(aresPacket);
    }

    @Override
    public void sendPlayerMsg(long uid, ByteBuf body) {
        AresTKcpContext channelHandlerContext = playerChannelContext.get(uid);
        if (channelHandlerContext == null) {
            log.error("uid = {}  not found in gateway", uid);
            return;
        }
        channelHandlerContext.send(body);
    }


    @Override
    public AresTKcpContext getRoleContext(long uid) {
        return playerChannelContext.get(uid);
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
        int online = playerChannelContext.size();
        myNodeInfo.setOnlineCount(online);
        discoveryService.getEtcdRegister().updateServerNodeInfo(myNodeInfo);
    }
}
