package com.ares.transport.inner;

import com.ares.core.bean.AresPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class InnerMsgEncoder extends MessageToByteEncoder<AresPacket> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AresPacket msg, ByteBuf out) {
        byte[] sendBody = msg.bodyEncode();
        byte[] sendHeader = msg.headerEncode();
        int msgLen = 1 + sendHeader.length;
        //-----4 bytes msg len |   2 bytes header len----| header |   body|
        if(sendBody != null){
            msgLen += sendBody.length;
        }

        out.writeInt(msgLen);
        out.writeByte(sendHeader.length);
        out.writeBytes(sendHeader);
        if (sendBody != null) {
            out.writeBytes(sendBody);
        }
        msg.release();
    }
}
