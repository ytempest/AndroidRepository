package com.ytempest.animationset.evaluator;

import android.animation.TypeEvaluator;

/**
 * @author ytempest
 *         Description：
 */
public class PointEvaluator implements TypeEvaluator<Point> {
    /**
     * 抛物线的真正X值
     */
    private float realX = -1;
    private Point point = new Point();

    /**
     * @param fraction   动画变化的速率，也就是插值器的返回值
     * @param startPoint 动画起始的圆点
     * @param endPoint   动画结束的圆点
     * @return 动画运行中的其中一个圆点
     */

    @Override
    public Point evaluate(float fraction, Point startPoint, Point endPoint) {
        if (realX == -1) {
            // 根据 y的变化范围获取 realX 的取值范围
            //（先转型为int的原因，防止得到三位小数0.001的值后，求根号函数出现错误）
            realX = (float) Math.sqrt((int) (endPoint.getY() - startPoint.getY()));
        }
        // 让原本的 x值按线性变化
        float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
        // 让 y值按抛物线变化
        float y = (float) Math.pow(realX * fraction, 2) + startPoint.getY();
        // 将计算后的坐标封装到一个新的Point对象中并返回
        point.updateXY(x, y);
        return point;
    }
}
