package com.ares.core.service;

import com.ares.core.bean.AresMsgIdMethod;

public interface IMsgCall {
    void onMethodInit(int msgId, AresMsgIdMethod aresMsgIdMethod);

    AresMsgIdMethod getCalledMethod(int msgId);
}
