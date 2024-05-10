package com.ares.transport.decoder;

import com.ares.transport.bean.NetWorkConstants;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class AresBasedFrameDecoder extends LengthFieldBasedFrameDecoder {

    public AresBasedFrameDecoder() {
        this(1024 * 1024 * 4, 0, NetWorkConstants.MSG_LEN_BYTES);
    }

    public AresBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
    }
}
