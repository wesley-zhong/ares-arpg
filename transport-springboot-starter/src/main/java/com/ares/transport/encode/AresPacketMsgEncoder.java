package com.ares.transport.encode;

import com.ares.core.bean.AresPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

//only send to client
@ChannelHandler.Sharable
public class AresPacketMsgEncoder extends MessageToByteEncoder<AresPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, AresPacket msg, ByteBuf out) {
        int sendHeaderLen = 0;
        byte[] sendBody = msg.bodyEncode();
        byte[] sendHeader = msg.headerEncode();
        int msgLen = 1 + sendHeader.length;
        if (sendBody != null) {
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
