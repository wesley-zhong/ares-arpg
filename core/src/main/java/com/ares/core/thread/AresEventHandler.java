package com.ares.core.thread;


import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AresEventHandler implements EventHandler<AresEventProcess> {
    @Override
    public void onEvent(AresEventProcess event, long sequence, boolean endOfBatch) {
        event.execute();
        event.clear();
    }
}
