package com.ares.core.bean;

import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


/**
 * create/release manually like ByteBuf
 */
@Slf4j
@Setter
@Getter
public class AresPacket {
    private Message sendBody;
    private Message sendHeader;
    private ByteBuf recvByteBuf;
    private ProtoCommon.MsgHeader recvHeader;

    public int getMsgId() {
        if (recvHeader != null) {
            return recvHeader.getMsgId();
        }
        return 0;
    }

    public AresPacket retain() {
        if (recvByteBuf != null) {
            recvByteBuf.retain();
        }
        return this;
    }

    public ProtoCommon.MsgHeader parseHeader() throws IOException {
        recvByteBuf.skipBytes(4);
        int headerLen = recvByteBuf.readByte();
        return recvHeader = ProtoCommon.MsgHeader.parseFrom(new ByteBufInputStream(recvByteBuf, headerLen));
    }

    public static AresPacket create(int msgId) {
        AresPacket aresPacket = new AresPacket();
        aresPacket.setSendHeader(ProtoCommon.MsgHeader.newBuilder()
                .setMsgId(msgId).build());

        return aresPacket;
    }

    public AresPacket copy() {
        AresPacket aresPacket = new AresPacket();
        if (this.recvByteBuf != null) {
            aresPacket.recvByteBuf = this.recvByteBuf.retain();
        }
        aresPacket.sendBody = this.sendBody;
        aresPacket.sendHeader = this.sendHeader;
        aresPacket.recvHeader = this.recvHeader;
        return aresPacket;
    }

    public static AresPacket create(Message header, Message body) {
        AresPacket aresPacket = new AresPacket();
        aresPacket.sendBody = body;
        aresPacket.sendHeader = header;
        return aresPacket;
    }

    public static AresPacket create(int msgId, Message body) {
        ProtoCommon.MsgHeader header = ProtoCommon.MsgHeader.newBuilder().setMsgId(msgId).build();
        return create(header, body);
    }


    public static AresPacket create() {
        return new AresPacket();
    }


    /**
     * not include msgId and msg len
     *
     * @return
     */
    public byte[] bodyEncode() {
        if (sendBody == null) {
            return null;
        }
        return sendBody.toByteArray();
    }

    public byte[] headerEncode() {
        if (sendHeader == null) {
            return null;
        }
        return sendHeader.toByteArray();
    }

    private void clear() {
        /***
         *  this package will be reused more times so do not clear data ,
         *  but when the recvByteBuf (this packet created by network read) is not null we should release it
         */
        if (recvByteBuf != null) {
            recvByteBuf.release();
            recvByteBuf = null;
        }
    }

    public void release() {
        // log.info("release object msg ={}", msgId);
        clear();
        //recyclerHandle.recycle(this);
    }

    private AresPacket() {
    }
}
