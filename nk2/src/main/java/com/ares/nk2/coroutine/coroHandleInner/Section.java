package com.ares.nk2.coroutine.coroHandleInner;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.StringFormatter;

public class Section {
    private final CoroHandle coroHandle;
    int action = CoroHandle.CoroHandleAction.CHA_DEFAULT;
    long startTimeNs = DateUtils.currentTimeNanos();
    long endTimeNs = DateUtils.currentTimeNanos();

    public Section(CoroHandle coroHandle) {
        this.coroHandle = coroHandle;
    }

    public long start(int action) {
        if (this.action != CoroHandle.CoroHandleAction.CHA_DEFAULT) {
            coroHandle.sectionEnd(this.action, null);
        }
        this.action = action;
        long nano = DateUtils.currentTimeNanos();
        long durationNs = nano - endTimeNs;
        startTimeNs = nano;
        endTimeNs = 0;
        return durationNs;
    }

    public long record(int action) {
        if (this.action != CoroHandle.CoroHandleAction.CHA_DEFAULT) {
            coroHandle.sectionEnd(this.action, null);
            return 0;
        }
        long nano = DateUtils.currentTimeNanos();
        long durationNs = nano - endTimeNs;
        startTimeNs = 0;
        endTimeNs = nano;
        return durationNs;
    }

    public long end(int action) {
        if (this.action != action) {
            CoroHandle.LOGGER.error(StringFormatter.format("end action when record action {} input action {}", this.action, action));
        }
        this.action = CoroHandle.CoroHandleAction.CHA_DEFAULT;
        long nano = DateUtils.currentTimeNanos();
        long durationNs = nano - startTimeNs;
        endTimeNs = nano;
        startTimeNs = 0;
        return durationNs;
    }
}
