package com.example.qxtx_win10.myapplication;

import android.graphics.Color;

/**
 * Created by QXTX-WIN10 on 2018/5/22.
 */

public class Tools {
    public final static float CATCH_MEMBER_SCALE = 2f; //捕获光点后放大的倍数
    public final static long CATCH_PRESSED_TIMEOUT = 1500L; //捕获光点后按住过长导致速率停止
    public final static float CATCH_MAKE_EASY = 20f; //捕获光点后按住过长导致速率停止

    public final static float DEFAULT_MEMBER_QUALITY = 1f; //光点质量
    public final static int DEFAULT_MEMBER_NUM = 100; //光点数量
    public final static float DEFAULT_MEMBER_SPEED = 1f; //光点运动速度（还和斜率有关）
    public final static float DEFAULT_MEMBER_RADIUS = 5f; //光点的大小
    public final static long SPEED_DOWN_MS = 3000L; //模拟阻尼运动，恢复默认速度的总耗时
    public final static float SPEED_SECOND_PER = 5f; //速度 SPEED px/0.0005s
    public final static float SLOPE_OFFSET_FIX = 0.2f; //撞到墙角的斜率偏移

    /**
     * 检查当前光点与其它光点是否可以连线
     * 返回： 连线的alpha值
     */
    public static int checkLine(int len) {
        if (len > 80) {
            return MyApplication.ALPHA_0;
        } else if (len > 72) {
            return MyApplication.ALPHA_1;
        } else if (len > 64) {
            return MyApplication.ALPHA_2;
        } else if (len > 56) {
            return MyApplication.ALPHA_3;
        } else if (len > 48) {
            return MyApplication.ALPHA_4;
        } else if (len > 40) {
            return MyApplication.ALPHA_5;
        } else if (len > 32) {
            return MyApplication.ALPHA_6;
        } else if (len > 16) {
            return MyApplication.ALPHA_7;
        } else if (len > 8) {
            return MyApplication.ALPHA_8;
        } else {
            return MyApplication.ALPHA_9;
        }
    }

    /**
     * 根据连结点的数量设置不同的颜色
     * 返回：color值
     */
    public static int checkColor(int lineCount) {
        switch (lineCount) {
            case 0:
                return Color.WHITE;
            case 1:
                return Color.parseColor("#00fff6");
            case 2:
                return Color.GREEN;
            default:
                return Color.RED;
        }
    }

    /**
     * 根据位移的加速度来得到速度（受控运动时发生）
     * 返回：speed等效值（SPEED px / 0.005s）
     */
    public static float checkSpeed(float deltaX, float deltaY, long deltaTime) {
        double delatLen = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        float speed;

        if (deltaTime == Long.MAX_VALUE) {
            speed = 0f;
        } else {
            speed = ((float)delatLen / (float)deltaTime) * Tools.SPEED_SECOND_PER;
        }

        return speed;  //  SPEEDpx/0.01s
    }

    //为速度补充矢量方向
    public static double[] fixHVspeedVector(Member member1, Member member2, double[] oldSpeed) {
        if ((member1.getSlope() > 0) == ( member2.getSlope() > 0)) { //slope同正负
            if (member2.getOrientation().equals(member1.getOrientation())) {
//                Log.e(TAG, "11");
                //nothing to change
            } else {
//                Log.e(TAG, "10");
                oldSpeed[0] = -oldSpeed[0];
                oldSpeed[1] = -oldSpeed[1];
            }
        } else { //slope异
            if (member2.getOrientation().equals(member1.getOrientation())) {
//                Log.e(TAG, "01");
                oldSpeed[0] = -oldSpeed[0];
                oldSpeed[1] = -oldSpeed[1];
            } else {
//                Log.e(TAG, "00");
                //nothing to change
            }
        }

        return oldSpeed;
    }

    //动量守恒 + 能量守恒得到速度
    public static double parseNewSpeed1(float m1, float m2, double v1, double v2) {
        return ((m1-m2)*v1+2.0*m2*v2) / (m1+m2);
    }
    public static double parseNewSpeed2(float m1, float m2, double v1, double v2) {
        return ((m2-m1)*v2+2.0*m1*v1) / (m1+m2);
    }

}
