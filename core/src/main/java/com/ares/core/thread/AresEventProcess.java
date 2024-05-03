package com.ares.core.thread;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AresEventProcess {
    private EventTask eventTask;
    public void execute(){
        eventTask.execute();
    }
    public void clear(){
        eventTask.clear();
    }
}
