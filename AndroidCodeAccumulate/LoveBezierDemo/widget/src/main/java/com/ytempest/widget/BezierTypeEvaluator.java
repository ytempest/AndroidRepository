package com.ytempest.widget;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * @author ytempest
 *         Description：贝塞尔估值器，用于实现贝塞尔曲线的效果
 */
public class BezierTypeEvaluator implements TypeEvaluator<PointF> {

    /**
     * 贝塞尔曲线的起点
     */
    private PointF point1;
    /**
     * 贝塞尔曲线的终点
     */
    private PointF point2;

    public BezierTypeEvaluator(PointF point1, PointF point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    /**
     * 根据 point0起点和 point3终点，计算出两点之间的贝塞尔曲线
     *
     * @param t      变化的范围，0~1
     * @param point0 起点
     * @param point3 终点
     * @return 在变化范围内的位于贝塞尔曲线上的一个点
     */
    @Override
    public PointF evaluate(float t, PointF point0, PointF point3) {

        PointF resultPoint = new PointF();

        resultPoint.x = (float) (point0.x * Math.pow((1 - t), 3)
                + 3 * point1.x * t * Math.pow((1 - t), 2)
                + 3 * point2.x * t * t * (1 - t)
                + point3.x * t * t * t);

        resultPoint.y = (float) (point0.y * Math.pow((1 - t), 3)
                + 3 * point1.y * t * Math.pow((1 - t), 2)
                + 3 * point2.y * t * t * (1 - t)
                + point3.y * t * t * t);

        return resultPoint;
    }
}
