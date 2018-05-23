package com.example.qxtx_win10.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 一个适合用作特效背景的surfaceView：网状变幻线
 * Created by QXTX-WIN10 on 2018/5/15.
 */
public final class SurfaceNetSky extends SurfaceView implements SurfaceHolder.Callback {
    public final static String TAG = "SurfaceRain";

    public final static int DEFAULT_FRAME_MS = 30;

    private SurfaceHolder mSurfaceHolder;
    private Type typeConfiguration; //动画配置类
    private long frameMs; //速率(ms)

    private boolean isRunning;
    private boolean endSurface;
    private ThreadDraw tStartDrawCycle;

    private CountDownLatch countDownLatch;

    public SurfaceNetSky(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        frameMs = DEFAULT_FRAME_MS;

        countDownLatch = new CountDownLatch(1);
        tStartDrawCycle = new ThreadDraw();
        endSurface = true;
        setReady(false);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        tStartDrawCycle.start();
    }

    public void setReady(boolean ready) {
        setRunning(ready);
        if (ready) {
            countDownLatch.countDown();
        }
    }

    public void end() {
        endSurface = false;
    }

    //获得和设置此控件的动画状态
    public boolean getRunning() {
        return isRunning;
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    //设置速率
    public long getFrameMs() {
        return frameMs;
    }
    public void setFrameMs(long frameMs) {
        this.frameMs = frameMs;
    }
    //view的获取和设置
    public Type getTypeConfiguration() {
        return typeConfiguration;
    }
    public void setTypeConfiguration(Type typeConfiguration) {
        this.typeConfiguration = typeConfiguration;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");

        setReady(false);
        end();
        if (tStartDrawCycle.isAlive()) {
            tStartDrawCycle.interrupt();
        }

        mSurfaceHolder.removeCallback(this);
        mSurfaceHolder = null;
        typeConfiguration = null;
    }

    //用于绘制的线程
    private class ThreadDraw extends Thread {
        private Canvas canvas;
        @Override
        public void run() {
            while(endSurface) {
                if (!isRunning) {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if ((tStartDrawCycle != null) && tStartDrawCycle.isAlive()) {
                    canvas = mSurfaceHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    typeConfiguration.onDraw(canvas);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                    typeConfiguration.onChanged(); //下一次改变

                    SystemClock.sleep(frameMs);
                }
            }
        }
    }
}
