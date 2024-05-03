package com.ares.core.thread;

import com.lmax.disruptor.EventFactory;

public class AresEventFactory implements EventFactory<AresEventProcess> {
    @Override
    public AresEventProcess newInstance() {
        return new AresEventProcess();
    }
}
