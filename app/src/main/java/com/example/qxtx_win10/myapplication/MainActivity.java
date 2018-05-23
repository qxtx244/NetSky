package com.example.qxtx_win10.myapplication;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.text.Format;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class MainActivity extends Activity {
    private SurfaceNetSky surfaceNetSky;
    private MemberConfiguration memberConfiguration;

    private float lastX = 0f, lastY = 0f;
    private int count = 0;
    private long[] historyTime = new long[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceNetSky = findViewById(R.id.netSky);
        surfaceNetSky.setZOrderOnTop(true);
        surfaceNetSky.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        //surfaceView已经测量完成，得到宽高，并添加按键的监听
        surfaceNetSky.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                memberConfiguration = new MemberConfiguration(surfaceNetSky.getMeasuredWidth(), surfaceNetSky.getMeasuredHeight());
                surfaceNetSky.setTypeConfiguration(memberConfiguration);
                surfaceNetSky.setReady(true);
                surfaceNetSky.setOnTouchListener(new OnTouchListener());
            }
        });
    }

    private boolean catchMember(MotionEvent event, boolean isCatchMember) {
        if (isCatchMember) {
            return true;
        }

        ArrayList<Member> members = memberConfiguration.getMembers();
        for (int i = 0; i < members.size(); i++) {
            if ((Math.abs(members.get(i).getX() - event.getRawX()) < Tools.CATCH_MAKE_EASY)
                    && (Math.abs(members.get(i).getY() - event.getRawY()) < Tools.CATCH_MAKE_EASY)) {

                members.get(i).setState(MyApplication.TYPE_FOLLOWED);
                members.get(i).setRadis(members.get(i).getRadis() * Tools.CATCH_MEMBER_SCALE);
                members.get(i).setColor(Color.WHITE);
                isCatchMember = true;
                break;
            }
        }

        return isCatchMember;
    }

    //如果isCatchMember已经是false，则没必要再设置
    private boolean freedomMember(boolean isCatchMember, MotionEvent event) {
        if (!isCatchMember) {
            return false;
        }

        ArrayList<Member> members = memberConfiguration.getMembers();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getState().equals(MyApplication.TYPE_FOLLOWED)) {
                //坐标点不可预料的改变导致轨迹方程式重新被定义
                float endX = event.getRawX();
                float endY = event.getRawY();
                long delataTime;

                count = 0; //松手后重置历史时间数组位置
                if (System.currentTimeMillis() - historyTime[1] > Tools.CATCH_PRESSED_TIMEOUT) { //按住的时间太长了
                    delataTime = Long.MAX_VALUE;
                } else {
                    delataTime = historyTime[1] - historyTime[0];
                }

                memberConfiguration.updateMemberSport(i, lastX, lastY, endX, endY, delataTime);

                members.get(i).setState(MyApplication.TYPE_FREEDOM);
                members.get(i).setRadis(members.get(i).getRadis() / Tools.CATCH_MEMBER_SCALE);
                isCatchMember = false;
                break;
            }
        }

        return isCatchMember;
    }

    private void moveMember(boolean isCatchMember, MotionEvent event) {
        if (!isCatchMember) {
            return ;
        }

        ArrayList<Member> members = memberConfiguration.getMembers();
        for (int i = 0; i < members.size(); i++) {
            String state = members.get(i).getState();
            if (state.equalsIgnoreCase(MyApplication.TYPE_FOLLOWED)) {
                //这里需要重新判断运动的轨迹，重新定义轨迹方程式
                lastX = members.get(i).getX();
                lastY = members.get(i).getY();

                if (count == historyTime.length) {
                    historyTime[0] = historyTime[1];
                    historyTime[1] = System.currentTimeMillis();
                    count = historyTime.length;
                } else {
                    historyTime[count] = System.currentTimeMillis();
                    count++;
                }

                members.get(i).setX(event.getRawX());
                members.get(i).setY(event.getRawY());

                break;
            }
        }
    }

    private class OnTouchListener implements View.OnTouchListener {
        private boolean isCatchMember = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isCatchMember = catchMember(event, isCatchMember);
                    break;
                case MotionEvent.ACTION_MOVE: //手机上的一次拖拽只执行了一次ACTION_MOVE
                    moveMember(isCatchMember, event);
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick(); //为了给onClick回调？
                    isCatchMember = freedomMember(isCatchMember, event);
                    break;
            }

            return false;
        }
    }
}
