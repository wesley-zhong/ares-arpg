package com.ares.login.network;


import com.ares.common.bean.ServerType;
import com.ares.core.bean.AresPacket;
import com.ares.transport.peer.PeerConnBase;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class PeerConn extends PeerConnBase {
    public void routerToTeam(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.ROUTER, uid, aresPacket);
    }

    public void routerGame(long uid, AresPacket aresPacket) {
        innerRedirectTo(ServerType.GAME, uid, aresPacket);
    }

    @Override
    public Channel loadBalance(int serverType, long uid) {
        return null;
    }
}
