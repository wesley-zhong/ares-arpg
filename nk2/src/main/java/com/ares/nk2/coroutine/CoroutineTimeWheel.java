package com.ares.nk2.coroutine;

import com.ares.nk2.container.IntrusiveList;
import com.ares.nk2.tool.DateUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;

public class CoroutineTimeWheel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineTimeWheel.class);
    private static final int wheelTime = 60000;
    private ArrayList<IntrusiveList<CoroWorker>> wheelArray = new ArrayList<>(wheelTime);
    private IntrusiveList<CoroWorker> moreThanOneMinList = new IntrusiveList<>("moreThanOneMinList");
    private int lastCheckIndex = 0;
    private long lastCheckLongTime = 0;
    private long lastProcTime = 0;

    CoroutineTimeWheel() {
        while (wheelArray.size() < wheelTime) {
            wheelArray.add(new IntrusiveList<>("timewheel-" + wheelArray.size()));
        }
    }

    void insert(CoroWorker coroWorker) {
        coroWorker.clearAllCoroList();
        long timeLeft = coroWorker.getJob().getParkInfo().getParkTimeout() - DateUtils.currentTimeMillis();
        if (timeLeft >= wheelTime) {
            moreThanOneMinList.insert(coroWorker.listElem);
        } else if (timeLeft < 0) {
            wheelArray.get(lastCheckIndex).insert(coroWorker.listElem);
        } else {
            int index = (int) (coroWorker.getJob().getParkInfo().getParkTimeout() % wheelTime);
            wheelArray.get(index).insert(coroWorker.listElem);
        }
    }

    private int increaseIndex(int index) {
        ++index;
        if (index >= wheelTime) {
            return 0;
        }
        return index;
    }

    private int tickIndex(int index) {
        int tickCount = 0;
        for (IntrusiveList.IntrusiveElem<CoroWorker> elem = wheelArray.get(index).head(); elem != wheelArray.get(index).end(); ) {
            IntrusiveList.IntrusiveElem<CoroWorker> curelem = elem;
            elem = elem.next();
            if (curelem.getData() != null &&
                    curelem.getData().getJob() != null &&
                    curelem.getData().getJob().getParkInfo().tryParkTimeout()) {
                CoroWorker worker = curelem.getData().getJob().getCoroWorker();
                worker.addReadyJobList();
                ++tickCount;
            }
        }
        return tickCount;
    }

    private void checkLongTimeout(long current) {
        int cnt = 0;
        for (IntrusiveList.IntrusiveElem<CoroWorker> elem = moreThanOneMinList.head(); elem != moreThanOneMinList.end(); ) {
            IntrusiveList.IntrusiveElem<CoroWorker> curelem = elem;
            elem = elem.next();
            ++cnt;

            if (curelem.getData() != null &&
                    curelem.getData().getJob() != null &&
                    curelem.getData().getJob().getParkInfo().getParkTimeout() - current < wheelTime) {
                curelem.remove();
                insert(curelem.getData());
            }
            if (cnt >= 500) {
                break;
            }
        }
    }

    public int proc() {
        if (lastProcTime >= DateUtils.currentTimeMillis()) {
            return 0;
        }
        int cnt = 0;
        lastProcTime = DateUtils.currentTimeMillis();
        int currentIndex = (int) (DateUtils.currentTimeMillis() % wheelTime);
        int index = lastCheckIndex;
        while (true) {
            cnt += tickIndex(index);
            if (index != currentIndex) {
                index = increaseIndex(index);
            } else {
                break;
            }
        }
        lastCheckIndex = increaseIndex(index);
        if (DateUtils.currentTimeMillis() - lastCheckLongTime >= (wheelTime / 3)) {
            lastCheckLongTime = DateUtils.currentTimeMillis();
            checkLongTimeout(lastCheckLongTime);
        }
        return cnt;
    }
}
