package com.example.qxtx_win10.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseIntArray;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Random;

/**
 * onDraw()中需要进行的事情：
 * 配置光点此时的参数
 * 画出每个光点
 * 配置光点连线的透明度
 * 画出光点之间的连线
 * Created by QXTX-WIN10 on 2018/5/19.
 */
public class MemberConfiguration implements Type {
    private final String TAG = "MemberConfiguration";

    private Paint paint;
    private ArrayList<Member> members; //光点成员
    private int memberNum; //光点个数
    private int windowWidth;
    private int windowHeight;

    MemberConfiguration(int windowWidth, int windowHeight) {
        memberNum = Tools.DEFAULT_MEMBER_NUM;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        members = new ArrayList<>();
        paint = new Paint();

        doSomeInit();
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawMembers(canvas);
        drawLine(canvas);
    }

    //光点运动
    @Override
    public void onChanged() {
        /**
         * 先判断斜率，再判断运动方向
         * 1、往左上走，则x和y都在减小，将会与左上墙壁碰撞，斜率大于0
         * 2、往右下走，则x和y都在增加，将会与右下墙壁碰撞，斜率大于0
         * 3、往左下走，则x在减小，y在增加，将会与左下墙壁碰撞，斜率小于0
         * 4、往右下走，则x在增加，y在减小，将会与右上墙壁碰撞，斜率小于0
         * 5、水平运动
         * 6、垂直运动
         */
        for (int i = 0; i < members.size(); i++) {
            if (!canMoveAuto(members.get(i).getState())) {
                continue;
            }

            //减速到正常速度
            if (members.get(i).getSpeed() > Tools.DEFAULT_MEMBER_SPEED) {
                members.get(i).setSpeed(members.get(i).getSpeed()
                        - (members.get(i).getSpeed() - Tools.DEFAULT_MEMBER_SPEED)
                        * SurfaceNetSky.DEFAULT_FRAME_MS / Tools.SPEED_DOWN_MS);
            }

            //增加碰撞检测
//            checkCollide(i);

            //光点运动越界检测
            maybeOverScreen(i);
        }

        for (int i = 0; i < members.size(); i++) {
            updateLines(i);
        }
    }

    private void maybeOverScreen(int i) {
        float slope = members.get(i).getSlope();
        String orientation = members.get(i).getOrientation();

        if ((slope == Float.MAX_VALUE) || (slope == Float.MIN_VALUE)) { //垂直运动
            move(i, false, orientation);
        } else if (slope > 0f) {
            if (orientation.equals(MyApplication.ORTENTATION[0])) {
                move(i, false, orientation); //往左上走
            } else if (orientation.equals(MyApplication.ORTENTATION[1])) {
                move(i, true, orientation);//往右下走
            }
        } else if (slope < 0f) {
            if (orientation.equals(MyApplication.ORTENTATION[0])) {
                move(i,true, orientation);//往右上走
            } else if (orientation.equals(MyApplication.ORTENTATION[1])) {
                move(i, false, orientation);//往左下走
            }
        } else { //水平方向走
            move(i, true, orientation);//往水平走
        }
    }

    //被按住的光点不能做游离运动
    private boolean canMoveAuto(String state) {
        return !state.equalsIgnoreCase(MyApplication.TYPE_FOLLOWED);
    }

    //绘制光点
    private void drawMembers(Canvas canvas) {
        int lineCount;
        float width;
        int alpha;
        int color;
        for (int i = 0; i < members.size(); i++) {
            lineCount = members.get(i).getLineList().size();
            width = members.get(i).getRadis();
            if (lineCount == 0) {
                alpha = 130;
                color = Color.WHITE;
            } else {
                alpha = new Random().nextInt(155) + 101;
                color = Tools.checkColor(lineCount);
            }

            paint.setAlpha(alpha);
            paint.setColor(color);
            paint.setStrokeWidth(width);
            members.get(i).setColor(color);

//            canvas.drawPoint(members.get(i).getX(), members.get(i).getY(), paint);
            canvas.drawCircle(members.get(i).getX(), members.get(i).getY(), members.get(i).getRadis(), paint);
        }
    }
    //绘制线条
    private void drawLine(Canvas canvas) {
        for (int i = 0; i < members.size(); i++) {
            Member cntMember = members.get(i);
            paint.setColor(cntMember.getColor());
            paint.setStrokeWidth(cntMember.getLineWidth());

            SparseIntArray cntLineList = cntMember.getLineList();
            for (int j = 0; j < cntLineList.size(); j++) { //需要找到对应id的光点的坐标
                Member anotherMember  = members.get(cntLineList.keyAt(j)); //拿到id，也就是num，光点的唯一序号
                float endX = (cntMember.getX() + anotherMember.getX()) / 2;
                float endY = (cntMember.getY() + anotherMember.getY()) / 2;

                paint.setAlpha(cntMember.getLineList().valueAt(j));
                canvas.drawLine(cntMember.getX(), cntMember.getY(), endX, endY, paint);
            }
        }
    }

    //配置光点之前做一些必要的检查
    private void doSomeInit() {
        int delta = members.size() - memberNum;
        if (delta == 0) {
            return ;
        }

        if (delta > 0) { //光点多了
            while (--delta >= 0) {
                members.remove(members.size() - 1); //移出最后一个光点
            }
        } else { //光点少了
            while (++delta <= 0) {
                members.add(new Member());
                membersValueInit(members.size() - 1);
//                membersValueInitTest(members.size() - 1);
            }
        }

        //更新连线
        for (int i = 0; i < members.size(); i++) {
            updateLines(i);
        }
    }

    //配置光点的属性
    private void membersValueInit(int num) {
        Member member = members.get(num);
        Random random = new Random();

        member.setId(num);
        member.setLineWidth(Tools.DEFAULT_MEMBER_RADIUS);
        member.setQuality(Tools.DEFAULT_MEMBER_QUALITY);
        member.setRadis(Tools.DEFAULT_MEMBER_RADIUS);
        member.setSpeed(Tools.DEFAULT_MEMBER_SPEED);
        member.setAlpha(MyApplication.ALPHA_9);

        float x = random.nextFloat() * windowWidth;
        float y = random.nextFloat() * windowHeight;

        member.setX(x);
        member.setY(y);

        //算出方程式
        float offset = member.getY() - member.getSlope() * member.getX();
        member.setOffset(offset);

        //斜率有正负，不可能是水平运动或者垂直运动
        float slope = (random.nextFloat() * 2f + 0.1f) * (random.nextInt(20) > 10 ? 1 : -1);
        member.setSlope(slope);

        //运动方向（初始化不会有垂直运动！）
        member.setOrientation(MyApplication.ORTENTATION[random.nextInt(MyApplication.ORTENTATION.length) % 2]);
    }

    private void membersValueInitTest(int num) {
        Member member = members.get(num);
        member.setLineWidth(Tools.DEFAULT_MEMBER_RADIUS);
        if (num == 0) {
            member.setX(0f);
            member.setY(1080f);
            member.setSlope(-1f);
            member.setId(num);
            member.setQuality(Tools.DEFAULT_MEMBER_QUALITY);
            member.setRadis(Tools.DEFAULT_MEMBER_RADIUS);
            member.setSpeed(Tools.DEFAULT_MEMBER_SPEED);
            member.setAlpha(MyApplication.ALPHA_9);
            member.setOffset(1080f);
            member.setOrientation(MyApplication.ORTENTATION[0]);
        } else {
            member.setX(200f);
            member.setY(1080f);
            member.setSlope(1f);
            member.setId(num);
            member.setQuality(Tools.DEFAULT_MEMBER_QUALITY);
            member.setRadis(Tools.DEFAULT_MEMBER_RADIUS);
            member.setSpeed(Tools.DEFAULT_MEMBER_SPEED);
            member.setAlpha(MyApplication.ALPHA_9);
            member.setOffset(880f);
            member.setOrientation(MyApplication.ORTENTATION[0]);
        }

//        Log.e("peek",  windowWidth  + ", " + windowHeight);
    }

    /** 光点的对撞
     * 判断撞击的光点个数（目前实现两点碰撞）
     * 撞击后位移方程式、速率都会发生改变
     * 猜测：同向可以合并光点，对撞可以分裂光点
     * 当受控速度很快时，忽略物理模型
     */
    private void checkCollide(int num) { //
        double[] speed1; //垂直/水平方向的初速度
        double[] speed2;
        double[] deltaSpeed = new double[2];

        Member cntMember = members.get(num);
        speed1 = getHVspeed(cntMember);

        for (int i = 0; i < members.size(); i++) {
            if (i == num) {
                continue;
            }

            float deltaX = members.get(i).getX() - cntMember.getX();
            float deltaY = members.get(i).getY() - cntMember.getY();
            float len = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            boolean isCollide = (len < (cntMember.getRadis() + members.get(i).getRadis()) / Math.sqrt(2));

            if (isCollide) {
                speed2 = getHVspeed(members.get(i));

                //为标量速度增加矢量方向 //现在不需要，本来就是矢量
                speed2 = Tools.fixHVspeedVector(cntMember, members.get(i), speed2);

                //配置两个相撞的光点新的轨迹参数
//                Log.e(TAG, "观察speed" + speed1[0] + ", " + speed2[0] + "\n" + speed1[1] + ", " + speed2[1]);
                deltaSpeed[0] = Tools.parseNewSpeed1(cntMember.getQuality(), members.get(i).getQuality(), speed1[0], speed2[0]); //第一个
                deltaSpeed[1] = Tools.parseNewSpeed1(cntMember.getQuality(), members.get(i).getQuality(), speed1[1], speed2[1]);
//                Log.e(TAG, "得到两个速度0:" + deltaSpeed[0] + ", " + deltaSpeed[1]);
                collideOne(cntMember, deltaSpeed[0], deltaSpeed[1]);
                deltaSpeed[0] = Tools.parseNewSpeed2(cntMember.getQuality(), members.get(i).getQuality(), speed1[0], speed2[0]); //第二个
                deltaSpeed[1] = Tools.parseNewSpeed2(cntMember.getQuality(), members.get(i).getQuality(), speed1[1], speed2[1]);
//                Log.e(TAG, "得到两个速度1:" + deltaSpeed[0] + ", " + deltaSpeed[1]);
                collideOne(members.get(i), deltaSpeed[0], deltaSpeed[1]);
            }
        }
    }

    //得到HV速度分量
    private double[] getHVspeed(Member member) {
        double[] speed = new double[2];
        if ((member.getSlope() == Float.MAX_VALUE) || (member.getSlope() == Float.MIN_VALUE)) {
            speed[0] = 0.0;
            speed[1] = (double)member.getSpeed();
        } else {
            double angle = Math.atan(member.getSlope());
            speed[0] = (double) member.getSpeed() * Math.cos(angle);
            speed[1] = (double) member.getSpeed() * Math.sin(angle);
        }

        return speed;
    }

    private void collideOne(Member cntMember, double deltaHspeed, double deltaVspeed) {
        String newOrtention;
        float newSlope, newOffset; //碰撞后产生的新参数
        float newSpeed = (float)Math.sqrt(deltaHspeed * deltaHspeed + deltaVspeed * deltaVspeed);

        if (deltaHspeed == 0) { //垂直，无意义
            newOffset = Float.MAX_VALUE;
            if (deltaVspeed > 0) { //向下走
                newSlope = Float.MAX_EXPONENT;
                newOrtention = MyApplication.ORTENTATION[1];
            } else if (deltaVspeed < 0) { //向上走
                newSlope = Float.MIN_VALUE;
                newOrtention = MyApplication.ORTENTATION[0];
            } else { //物体静止
                newSlope = new Random().nextFloat();
                newOrtention = MyApplication.ORTENTATION[3]; //静止
            }
        } else {
            newSlope = (float)(deltaVspeed / deltaHspeed);
            newOffset = cntMember.getY() - newSlope * cntMember.getX();
            if (deltaVspeed > 0) {
                newOrtention = MyApplication.ORTENTATION[1];
            } else if (deltaVspeed < 0) {
                newOrtention = MyApplication.ORTENTATION[0];
            } else {
                newOrtention = MyApplication.ORTENTATION[2];
            }
        }

//        Log.e(TAG, "新的newSlope：" + newSlope + ", newOriention：" + newOrtention);
        cntMember.setSpeed(newSpeed);
        cntMember.setSlope(newSlope);
        cntMember.setOffset(newOffset);
        cntMember.setOrientation(newOrtention);
    }

    /** 光点运动
     * 受控的光点无法自由运动，如被按住的光点
     * 发生非墙角碰撞：斜率变成倒数的同时正负互换
     * 发生墙角碰撞：斜率稍微偏移避开原路运动
     * 斜率改变后，运动方向也随之改变，需要重新配置
     * 斜率改变后，offset也必然随之改变，需要重新配置
     */
    private void move(int num, @Nullable boolean isAddX, String ortiention) {
        Member cntMember = members.get(num);
        float endX, endY, offset;

        if (ortiention.equals(MyApplication.ORTENTATION[3])) { //优先处理垂直走的问题
            if (cntMember.getSlope() == Float.MAX_VALUE) { //向下走
                endY = cntMember.getY() + cntMember.getSpeed();
                if (endY >= windowHeight) { //撞到底部
                    cntMember.setY(windowHeight);
                    cntMember.setSlope(5f * (new Random().nextInt(2) == 0 ? 1 : -1));
                    cntMember.setOffset(cntMember.getY() - cntMember.getSlope() * cntMember.getX());
                    cntMember.setOrientation(MyApplication.ORTENTATION[0]);
                } else { //在区域内活动
                    cntMember.setY(endY);
                }
            } else if (cntMember.getSlope() == Float.MIN_VALUE) { //向上走
                endY = cntMember.getY() - cntMember.getSpeed();
                if (endY <= 0f) { //撞到顶部
                    cntMember.setY(0f);
                    cntMember.setSlope(5f * (new Random().nextInt(2) == 0 ? 1 : -1));
                    cntMember.setOffset(cntMember.getY() - cntMember.getSlope() * cntMember.getX());
                    cntMember.setOrientation(MyApplication.ORTENTATION[1]);
                } else {
                    cntMember.setY(endY);
                }
            }
            return ;
        } else if (ortiention.equalsIgnoreCase(MyApplication.ORTENTATION[2])) { //水平走
            if (isAddX) { //水平向右走
                endX = cntMember.getX() + cntMember.getSpeed(); //x以单位速率来变化（speed/frameMs）
                if (endX >= windowWidth) { //撞到右边
                    cntMember.setX(windowWidth);
                    cntMember.setSlope(2f * (new Random().nextInt(2) == 0 ? 1 : -1));
                    cntMember.setOffset(cntMember.getY() - cntMember.getSlope() * cntMember.getX());
                    if (cntMember.getSlope() > 0) {
                        cntMember.setOrientation(MyApplication.ORTENTATION[1]);
                    } else {
                        cntMember.setOrientation(MyApplication.ORTENTATION[0]);
                    }
                } else {
                    cntMember.setX(endX);
                }
            } else { //水平向左走
                endX = cntMember.getX() - cntMember.getSpeed();
                if (endX <= 0f) { //撞到左边
                    cntMember.setX(0f);
                    cntMember.setSlope(2f * (new Random().nextInt(2) == 0 ? 1 : -1));
                    cntMember.setOffset(cntMember.getY() - cntMember.getSlope() * cntMember.getX());
                    if (cntMember.getSlope() > 0) {
                        cntMember.setOrientation(MyApplication.ORTENTATION[1]);
                    } else {
                        cntMember.setOrientation(MyApplication.ORTENTATION[0]);
                    }
                } else {
                    cntMember.setX(endX);
                }
            }

            return ;
        }

        boolean isAddY = ortiention.equals(MyApplication.ORTENTATION[1]);

        //往左上走， 斜率>0，X 和 Y 都在减小
        if (!isAddX && !isAddY) {
            endX = cntMember.getX() - cntMember.getSpeed(); //x以单位速率来变化（speed/frameMs）
            endY = cntMember.getSlope() * endX + cntMember.getOffset();

            if ((endX <= 0f) && (endY > 0f)) { //撞到左边了
                cntMember.setX(0f);
                endY = cntMember.getSlope() * cntMember.getX() + cntMember.getOffset();
                cntMember.setY(endY);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getSlope() * cntMember.getX();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[0]);
            } else if ((endX > 0f) && (endY <= 0f)) { //撞到顶部了
                cntMember.setY(0f);
                endX = (cntMember.getY() - cntMember.getOffset()) / cntMember.getSlope();
                cntMember.setX(endX);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[1]);
            } else if ((endX > 0f) && (endY > 0f)) { //没有撞到墙壁
                cntMember.setX(endX);
                cntMember.setY(endY);
            } else { //撞到墙角 即x = 0f，y=0f
                cntMember.setX(endX);
                cntMember.setY(endY);
                cntMember.setOrientation(MyApplication.ORTENTATION[1]);
                cntMember.setSlope(cntMember.getSlope() + Tools.SLOPE_OFFSET_FIX); //避开墙角循环！增加斜率偏移
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
            }

            //往右下走，斜率>0，X 和 Y 都在增加
        } else if (isAddX && isAddY) {
            endX = cntMember.getX() + cntMember.getSpeed();
            endY = cntMember.getSlope() * endX + cntMember.getOffset();

            if ((endX >= windowWidth) && (endY < windowHeight)) { //撞到右边了
                cntMember.setX(windowWidth);
                endY = cntMember.getSlope() * cntMember.getX() + cntMember.getOffset();
                cntMember.setY(endY);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[1]);
            } else if ((endX < windowWidth) && (endY >= windowHeight)) { //撞到底部了
                cntMember.setY(windowHeight);
                endX = (cntMember.getY() - cntMember.getOffset()) / cntMember.getSlope();
                cntMember.setX(endX);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[0]);
            } else if ((endX < windowWidth) && (endY < windowHeight)) { //没有撞到墙壁
                cntMember.setX(endX);
                cntMember.setY(endY);
            } else { //撞到墙角，即x = windowWidth，y = windowHeight
                cntMember.setX(endX);
                cntMember.setY(endY);
                cntMember.setOrientation(MyApplication.ORTENTATION[0]);
                cntMember.setSlope(cntMember.getSlope() + Tools.SLOPE_OFFSET_FIX);
            }

            //往右上走，斜率<0，X 在增加，Y 在减小
        } else if (isAddX && !isAddY) {
            endX = cntMember.getX() + cntMember.getSpeed(); //x以单位速率来变化（speed/frameMs）
            endY = cntMember.getSlope() * endX + cntMember.getOffset();

            if ((endX >= windowWidth) && (endY > 0f)) { //撞到右边了
                cntMember.setX(windowWidth);
                endY = cntMember.getSlope() * cntMember.getX() + cntMember.getOffset();
                cntMember.setY(endY);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[0]);
            } else if ((endX < windowWidth) && (endY <= 0f)) { //撞到顶部了
                cntMember.setY(0f);
                endX = (cntMember.getY() - cntMember.getOffset()) / cntMember.getSlope();
                cntMember.setX(endX);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[1]);
            } else if ((endX < windowWidth) && (endY < windowHeight)) { //没有撞到墙壁
                cntMember.setX(endX);
                cntMember.setY(endY);
            } else { //撞到墙角 即x = windowWidth, y = 0f
                cntMember.setX(endX);
                cntMember.setY(endY);
                cntMember.setOrientation(MyApplication.ORTENTATION[1]);
                cntMember.setSlope(cntMember.getSlope() + Tools.SLOPE_OFFSET_FIX);
            }

            //往左下走，斜率<0，X 在减小，Y在增加
        } else if (!isAddX && isAddY) {
            endX = cntMember.getX() - cntMember.getSpeed(); //x以单位速率来变化（speed/frameMs）
            endY = cntMember.getSlope() * endX + cntMember.getOffset();

            if ((endX <= 0f) && (endY < windowHeight)) { //撞到左边了
                cntMember.setX(0f);
                endY = cntMember.getSlope() * cntMember.getX() + cntMember.getOffset();
                cntMember.setY(endY);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[1]);
            } else if ((endX > 0f) && (endY >= windowHeight)) { //撞到底部了
                cntMember.setY(windowHeight);
                endX = (cntMember.getY() - cntMember.getOffset()) / cntMember.getSlope();
                cntMember.setX(endX);
                cntMember.setSlope((1f / cntMember.getSlope()) * (-1f));
                offset = cntMember.getY() - cntMember.getX() * cntMember.getSlope();
                cntMember.setOffset(offset);
                cntMember.setOrientation(MyApplication.ORTENTATION[0]);
            } else if ((endX < windowWidth) && (endY < windowHeight)) { //没有撞到墙壁
                cntMember.setX(endX);
                cntMember.setY(endY);
            } else { //撞到墙角 即x = 0f, y = windowHeight
                cntMember.setX(endX);
                cntMember.setY(endY);
                cntMember.setOrientation(MyApplication.ORTENTATION[0]);
                cntMember.setSlope(cntMember.getSlope() + Tools.SLOPE_OFFSET_FIX);
            }
        }
    }

    //刷新连线和光点状态
    private void updateLines(int num) {
        Member cntMember = members.get(num);
        SparseIntArray cntLineList = cntMember.getLineList();
        cntLineList.clear();//每次重新计算连线组合
        float x = cntMember.getX();
        float y = cntMember.getY();
        for (int i = 0; i < members.size(); i++) {
            if (i != num) { //排除自己
                float deltaX = x - members.get(i).getX();
                float deltaY = y - members.get(i).getY();
                int lineLen = (int)Math.sqrt((deltaX * deltaX + deltaY * deltaY));
                int alphaResult = Tools.checkLine(lineLen); //返回线段透明度
                if (alphaResult != MyApplication.ALPHA_0) { //光点距离太远将不会连线
                    cntLineList.append(members.get(i).getId(), alphaResult);
                }
            }
        }

        //光点的状态
        Tools.checkColor(cntLineList.size());
        if ((cntMember.getState() == null) || (!cntMember.getState().equalsIgnoreCase(MyApplication.TYPE_FOLLOWED))) {
            if (cntLineList.size() > 0) {
                cntMember.setState(MyApplication.TYPE_LINE);
            } else {
                cntMember.setState(MyApplication.TYPE_FREEDOM);
            }
        }
    }

    //更新光点的轨迹方程式
    public void updateMemberSport(int memberNum, float startX, float startY, float endX, float endY, long deltaTime) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float slope;
        String oriention;
        float offset;
        float speed;

        if (deltaX == 0f) {  //竖直运动
            slope = deltaY > 0f ? Float.MAX_VALUE : Float.MIN_VALUE; //区分向上和向下
            offset = Float.MAX_VALUE;
            oriention = MyApplication.ORTENTATION[3];
            members.get(memberNum).setSlope(slope);
            members.get(memberNum).setOffset(offset);
            members.get(memberNum).setOrientation(oriention);
            speed = Tools.checkSpeed(deltaX, deltaY, deltaTime);
            members.get(memberNum).setSpeed(speed);
            return;
        } else {
            slope = deltaY / deltaX;
        }

        offset = endY - slope * endX;

        if (deltaY > 0f) {
            oriention = MyApplication.ORTENTATION[1];
        } else if (deltaY < 0f) {
            oriention = MyApplication.ORTENTATION[0];
        } else {
            oriention = MyApplication.ORTENTATION[2];
        }

        members.get(memberNum).setSlope(slope);
        members.get(memberNum).setOffset(offset);
        members.get(memberNum).setOrientation(oriention);

        speed = Tools.checkSpeed(deltaX, deltaY, deltaTime);
        members.get(memberNum).setSpeed(speed);
    }

    public ArrayList<Member> getMembers() {
        return members;
    }
    public void setMembers(ArrayList<Member> members) {
        if ((members != null) && (members.size() > 0)) {
            this.members.clear();
            this.members.addAll(members);
            memberNum = this.members.size();
            doSomeInit();
        }
    }

    public int getMemberNum() {
        return memberNum;
    }
    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
        doSomeInit();
    }
}
