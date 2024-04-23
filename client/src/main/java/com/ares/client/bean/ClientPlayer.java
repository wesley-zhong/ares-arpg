package com.ares.client.bean;

import com.ares.core.bean.AresPacket;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPlayer {
    private long uId;
    private Channel context;

    public ClientPlayer(long uId) {
        this.uId = uId;
        this.context = context;
    }


    public void send(int msgId, Message body) {
        this.context.writeAndFlush(AresPacket.create(msgId, body));
    }
    public void send(int msgId){
        send(msgId, null);
    }

    public void send(ProtoCommon.MsgHeader header, Message body) {
        this.context.writeAndFlush(AresPacket.create(header, body));
    }

    public void send(AresPacket aresPacket){
        this.context.writeAndFlush(aresPacket);
    }
}
