package com.ytempest.animationset.evaluator;

/**
 * @author ytempest
 *         Description：
 */
public class Point {

    /** 设置两个变量用于记录坐标的位置 */
    private float x;
    private float y;

    public Point() {
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * get方法用于获取坐标
     */
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void updateXY(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
