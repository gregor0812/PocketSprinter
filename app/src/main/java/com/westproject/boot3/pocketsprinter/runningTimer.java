package com.westproject.boot3.pocketsprinter;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.lang.ref.WeakReference;

/**
 * Created by Gregor on 17/05/2017.
 * This class handles the actual running timer for the runningActivity
 */

public abstract class runningTimer {
    public abstract void onTick(long millisUntilFinished);

    public abstract void onFinish();

    public runningTimer(long millisInFuture, long countDownInterval) {
        timerLife = millisInFuture;
        period = countDownInterval;
    }

    public synchronized runningTimer start() {
        if (cancelled) return this;
        paused = false;
        if (timerLife <= 0) {
            onFinish();
        } else {
            destTime = SystemClock.elapsedRealtime() + timerLife;
            handler.sendEmptyMessage(MESSAGE_WHAT);
        }
        return this;
    }

    public synchronized void pause() {
        if (paused || cancelled) return;
        timerLife = destTime - SystemClock.elapsedRealtime();
        paused = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public synchronized void cancel() {
        cancelled = true;
        handler.removeMessages(MESSAGE_WHAT);
    }

    private static final class runningHandler extends Handler {
        public runningHandler(runningTimer that) {
            weakThat = new WeakReference<>(that);
        }

        @Override
        @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
        public void handleMessage(Message m) {
            runningTimer that = weakThat.get();
            if (that == null) return;
            synchronized (that) {
                if (that.paused || that.cancelled) return;

                long lastTick = SystemClock.elapsedRealtime();
                long millisLeft = that.destTime - lastTick;
                if (millisLeft <= 0) {
                    that.onFinish();
                } else if (millisLeft < that.period) {
                    sendEmptyMessageDelayed(MESSAGE_WHAT, millisLeft);
                } else {
                    that.onTick(millisLeft);
                    if (that.cancelled || that.paused) return;
                    long delay = lastTick + that.period - SystemClock.elapsedRealtime();
                    while (delay < 0) delay += that.period;
                    sendEmptyMessageDelayed(MESSAGE_WHAT, delay);
                }
            }
        }

        private WeakReference<runningTimer> weakThat;
    }

    private Handler handler = new runningHandler(this);

    private final static int MESSAGE_WHAT = 42;
    private boolean cancelled = false;
    private boolean paused = true;

    protected final long period;
    // Use timerLife before we start or when resuming from pause
    protected long timerLife;
    // Use destTime when timer is running
    protected long destTime;
}
