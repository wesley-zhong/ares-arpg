package com.ares.team.bean.timer;

import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.core.timer.AresTimerTask;
import com.ares.core.timer.ScheduleService;
import com.ares.team.configuration.ThreadPoolType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


@Component
public class TimerEventHandler implements InitializingBean {
    private ScheduleService scheduleService;

    public void onTimerTask(AresTimerTask aresTimerTask) {
        AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.selectThreadPool(ThreadPoolType.LOGIC.getValue());
        logicProcessThreadPool.execute(aresTimerTask.getExecuteHashCode(), aresTimerTask, (timerTask) -> {
            if (timerTask.isValid()) {
                timerTask.getCall().apply(aresTimerTask.getExtData());
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduleService = new ScheduleService(this::onTimerTask);
    }
}
