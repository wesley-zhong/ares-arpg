package com.ares.client;

import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

@Component
public class LoginService {


    public void loginRequest(Channel channel, long uid) {
        ProtoGame.GameLoginReq.Builder loginRequest = ProtoGame.GameLoginReq.newBuilder()
                .setUid(6666)
                .setGameToken("token");

        AresPacket aresPacket = AresPacket.create(ProtoCommon.ProtoCode.GAME_LOGIN_REQ_VALUE, loginRequest.build());
        channel.writeAndFlush(aresPacket);
    }
}
