package com.ytempest.animationset.evaluator;

import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.util.Log;

/**
 * @author ytempest
 *         Description：
 */
public class ColorEvaluator implements TypeEvaluator<Integer> {

    private boolean isInit = false;
    private int startAlpha;
    private int startRed;
    private int startGreen;
    private int startBlue;

    @Override
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {

        if (!isInit) {
            initValue(startValue);
            isInit = true;
        }
        int finalAlpha = (int) ((Color.alpha(endValue) - startAlpha) * fraction + startAlpha);
        int finalRed = (int) ((Color.red(endValue) - startRed) * fraction + startRed);
        int finalGreen = (int) ((Color.green(endValue) - startGreen) * fraction + startGreen);
        int finalBlue = (int) ((Color.blue(endValue) - startBlue) * fraction + startBlue);
        return Color.argb(finalAlpha, finalRed, finalGreen, finalBlue);
    }

    private void initValue(Integer startValue) {
        startAlpha = Color.alpha(startValue);
        startRed = Color.red(startValue);
        startGreen = Color.green(startValue);
        startBlue = Color.blue(startValue);
    }

}
