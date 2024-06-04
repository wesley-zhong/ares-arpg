package com.ares.core.event;

public enum EventDispatchResult {
    /*分发完成*/
    Done,
    /*排队分发*/
    Queue,
    /*分发错误*/
    Error,
    /*忽略事件*/
    Ignore,
}
