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
    private int alphaDiff;
    private int redDiff;
    private int greenDiff;
    private int blueDiff;

    @Override
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {

        if (!isInit) {
            initValue(startValue, endValue);
            isInit = true;
        }
        int finalAlpha = (int) Math.abs(startAlpha + (alphaDiff * fraction));
        int finalRed = (int) Math.abs(startRed + (redDiff * fraction));
        int finalGreen = (int) Math.abs(startGreen + (greenDiff * fraction));
        int finalBlue = (int) Math.abs(startBlue + (blueDiff * fraction));
        Log.e("TAG", "finalAlpha="+finalAlpha + "  ||  finalRed=" + finalRed + "  ||  finalGreen=" + finalGreen + "  ||  finalBlue=" + finalBlue);
        return Color.argb(finalAlpha, finalRed, finalGreen, finalBlue);
    }

    private void initValue(Integer startValue, Integer endValue) {
        startAlpha = Color.alpha(startValue);
        startRed = Color.red(startValue);
        startGreen = Color.green(startValue);
        startBlue = Color.blue(startValue);
        alphaDiff = Color.alpha(endValue) - startAlpha;
        redDiff = Color.red(endValue) - startRed;
        greenDiff = Color.green(endValue) - startGreen;
        blueDiff = Color.blue(endValue) - startBlue;
    }

}
