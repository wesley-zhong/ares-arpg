package com.ares.client;

import com.ares.client.bean.ClientPlayer;
import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

@Component
public class LoginService {

    public void loginRequest(ClientPlayer clientPlayer) {
        ProtoGame.GameLoginReq.Builder loginRequest = ProtoGame.GameLoginReq.newBuilder()
                .setUid(clientPlayer.getUId())
                .setGameToken("token");
        clientPlayer.send(ProtoCommon.MsgId.GAME_LOGIN_REQ_VALUE,loginRequest.build());
    }
}
