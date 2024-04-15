package com.ares.client.performance;

import com.ares.client.Client;
import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PerformanceTestService {

    @Autowired
    private Client client;
    public void  startSend (){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < Integer.MAX_VALUE; i++) {
                        ProtoGame.DirectToWorldReq req = ProtoGame.DirectToWorldReq.newBuilder().setResBody("OOOOOOOOOOOOOOOOO").setSomeId(13223333).build();
                        AresPacket directWorld = AresPacket.create(ProtoCommon.ProtoCode.DIRECT_TO_TEAM_REQ_VALUE, req);
                        client.getChannel().writeAndFlush(directWorld);
                        Thread.sleep(10);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }
}
