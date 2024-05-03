package com.ares.nk2.coroutine.coroHandleInner;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.tool.DateUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class ParkInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkInfo.class);
    private final CoroHandle coroHandle;
    public Object parkVerifyObj = null;
    public AtomicBoolean isUnparking = new AtomicBoolean(false);
    private long parkTime = 0;
    private long parkTimeout = 0;
    private boolean isTimeout = false;
    private long resumeTime = 0;
    private boolean interrupted = false;
    private Throwable parkStack;

    public ParkInfo(CoroHandle coroHandle) {
        this.coroHandle = coroHandle;
    }

    private void clearPark() {
        parkTime = 0;
        parkTimeout = 0;
        isTimeout = false;
        parkVerifyObj = null;
        resumeTime = 0;
        interrupted = false;
        parkStack = null;
    }

    public void finiUnpark() {
        clearPark();
    }

    public boolean tryParkTimeout() {
        if (coroHandle.coroBaseInfo.status == CoroHandle.CoroJobStatus.PARKED) {
            if (parkTimeout > 0 && parkTimeout <= DateUtils.currentTimeMillis()) {
                isTimeout = true;
                interrupted = false;
                if (parkVerifyObj != null && parkStack != null) {
                    LOGGER.error("", parkStack);
                }
                parkStack = null;
                return true;
            }
        }
        return false;
    }

    public void setPark(Object parkVerifyObj, long timeout) {
        clearPark();
        this.parkVerifyObj = parkVerifyObj;
        parkTime = DateUtils.currentTimeMillis();
        if (timeout == 0) {
            parkTimeout = 0;
        } else {
            parkTimeout = timeout + parkTime;
        }
        coroHandle.sectionStart(CoroHandle.CoroHandleAction.CHA_PARK);
    }

    public void setUnPark() {
        isUnparking.set(false);
        resumeTime = DateUtils.currentTimeMillis();
        coroHandle.sectionStart(CoroHandle.CoroHandleAction.CHA_RESUME);
        parkStack = null;
    }

    public long getParkTimeout() {
        return parkTimeout;
    }

    public long getParkTime() {
        return parkTime;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        isTimeout = false;
        this.interrupted = interrupted;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    @SuppressWarnings("unused")
    public long getResumeTime() {
        return resumeTime;
    }
}
