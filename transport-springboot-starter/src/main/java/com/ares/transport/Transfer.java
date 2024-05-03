package com.ares.transport;

import com.google.protobuf.Message;

public interface Transfer {
    void sendMsg(int msgId, Message body);
}
