package com.router.contoller;

import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoCommon;
import com.router.network.PeerConn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProxyService {
    @Autowired
    private PeerConn peerConn;

    public void proxyMsg(long uid, AresPacket aresPacket) {
        ProtoCommon.MsgHeader msgHeader = aresPacket.getRecvHeader();
        int msgId = msgHeader.getMsgId();
        int toServerType = msgHeader.getRouterTo();
        log.info(" proxy uid = {} msgId ={} to server type ={}", uid, msgId, toServerType);
        try {
            if (toServerType == ServerType.TEAM.getValue()) {
                peerConn.directToTeam(uid, aresPacket);
                return;
            }
            if (toServerType == ServerType.GAME.getValue()) {
                peerConn.directToGame(uid, aresPacket);
                return;
            }
            log.error("XXXXXXXXXXXXXXX from uid  ={}  msgId ={} to server type ={} error", uid, msgId, toServerType);
        } finally {
            aresPacket.release();
        }
    }

}
