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
    private float readX = -1;

    @Override
    public Point evaluate(float fraction, Point startPoint, Point endPoint) {
        if (readX == -1) {
            // 根据 y的变化范围获取 readX 的取值范围
            //（先转型为int的原因，防止得到三位小数0.001的值后，求根号函数出现错误）
            readX = (float) Math.sqrt((int) (endPoint.getY() - startPoint.getY()));
        }
        // 让原本的 x值按线性变化
        float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
        // 让 y值按抛物线变化
        float y = (float) Math.pow(readX * fraction, 2) + startPoint.getY();
        // 将计算后的坐标封装到一个新的Point对象中并返回
        return new Point(x, y);
    }
}
