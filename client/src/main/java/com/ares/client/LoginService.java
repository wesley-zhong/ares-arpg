package com.ares.client;

import com.ares.client.bean.ClientPlayer;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoMsgId;
import org.springframework.stereotype.Component;

@Component
public class LoginService {

    public void loginRequest(ClientPlayer clientPlayer) {
        ProtoGame.GameLoginPush.Builder loginRequest = ProtoGame.GameLoginPush.newBuilder()
                .setUid(clientPlayer.getUId())
                .setGameToken("token");
        clientPlayer.send(ProtoMsgId.MsgId.GAME_LOGIN_PUSH_VALUE,loginRequest.build());
    }
}
