package com.ytempest.animationset.interpolator;

import android.view.animation.Interpolator;

/**
 * @author ytempest
 *         Description：
 */
public class DecelerateAccelerateInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        // input的运算逻辑如下，使用了x^2函数
        // 在这里x^2函数中的x取值范围限定在0~2，那么y值的范围就是0~4。
        // 通过查看x^2函数x值在0~2的图像可以知道该差值器是先减速再加速的
        return (float) (Math.pow((2 * input), 2)) / 4.0f;
    }
}
