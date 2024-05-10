package com.ares.nk2.timer;

import com.ares.nk2.tool.NKStringFormater;

public class TimerList {
//    private static final Logger LOGGER = LoggerFactory.getLogger(TimerList.class);

    private final TimerHandle head;

    public TimerList() {
        head = new TimerHandle();
        head.prev = head;
        head.next = head;
    }

    public void addTimerBack(TimerHandle timer) {
        if (timer.next != null || timer.prev != null) {
//            removeTimer(timer);
            throw new IllegalStateException(NKStringFormater.format("timer-{} already in list, can not add again", timer));
        }
        TimerHandle tail = head.prev;
        timer.next = head;
        timer.prev = tail;
        tail.next = timer;
        head.prev = timer;
    }

    public void addTimerFront(TimerHandle timer) {
        if (timer.next != null || timer.prev != null) {
//            removeTimer(timer);
            throw new IllegalStateException(NKStringFormater.format("timer-{} already in list, can not add again", timer));
        }
        TimerHandle head = this.head.next;
        timer.next = head;
        timer.prev = this.head;
        head.prev = timer;
        this.head.next = timer;
    }


    public static void removeTimer(TimerHandle timer) {
        if (timer.next != null) {
            timer.next.prev = timer.prev;
        }
        if (timer.prev != null) {
            timer.prev.next = timer.next;
        }

        timer.next = null;
        timer.prev = null;

    }


    public TimerHandle peek() {
        if (isEmpty()) {
            return null;
        }
        return head.next;
    }

    public TimerHandle poll() {
        if (isEmpty()) {
            return null;
        } else {
            TimerHandle firstTimer = head.next;
            removeTimer(firstTimer);
            return firstTimer;
        }
    }

    public TimerHandle pollTail() {
        if (isEmpty()) {
            return null;
        } else {
            TimerHandle lastTimer = head.prev;
            removeTimer(lastTimer);
            return lastTimer;
        }
    }

    public boolean isEmpty() {
        return head.next == head;
    }

    public void addExpiredTimer(TimerHandle timer) {
        TimerHandle base = head.next;
        while (base != head && timer.getExpireTick() >= base.getExpireTick()) {
            base = base.next;
        }
        timer.next = base;
        timer.prev = base.prev;
        base.prev.next = timer;
        base.prev = timer;
    }
}
