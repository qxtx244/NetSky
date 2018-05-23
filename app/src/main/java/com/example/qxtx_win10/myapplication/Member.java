package com.example.qxtx_win10.myapplication;

import android.util.SparseIntArray;

/**
 * 光点的属性集
 * Created by QXTX-WIN10 on 2018/5/19.
 */
public class Member {
    private int id;
    private float quality; //质量
    private float x;
    private float y;
    private float radis; //光点的半径大小
    private int alpha; //光点的透明
    private int color; //光点、连线的颜色
    private float speed; //光点运动速度
    private float lineWidth; //连线宽度，目前强制保持为1px
    private String state; //光点状态 "freedom":游离状态  "line":连线状态  "follow":跟随触点状态
    private float slope; //斜率
    private SparseIntArray lineList; //key：光点的id    value：光点的连线alpha值
    private String orientation;
    private float offset; //y = kx + offset;

    public Member() {
        lineList = new SparseIntArray();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public float getQuality() {
        return quality;
    }
    public void setQuality(float quality) {
        this.quality = quality;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getRadis() {
        return this.radis;
    }
    public void setRadis(float radis) {
        this.radis = radis;
    }

    public int getAlpha() {
        return alpha;
    }
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }

    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getLineWidth() {
        return lineWidth;
    }
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public float getSlope() {
        return slope;
    }
    public void setSlope(float slope) {
        this.slope = slope;
    }

    public SparseIntArray getLineList() {
        return lineList;
    }
    public void setLineList(SparseIntArray lineList) {
        this.lineList = lineList;
    }

    public String getOrientation() {
        return orientation;
    }
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public float getOffset() {
        return offset;
    }
    public void setOffset(float offset) {
        this.offset = offset;
    }
}
