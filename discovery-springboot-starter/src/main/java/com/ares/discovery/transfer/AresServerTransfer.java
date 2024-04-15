package com.ares.discovery.transfer;

import com.ares.core.bean.AresPacket;
import com.google.protobuf.Message;

public interface AresServerTransfer {
    void sendMsg(int msgId, Message body);

    void sendMsg(int areaId, int msgId, Message body);

    void sendMsg(int areaId, AresPacket  aresPacket);
}
