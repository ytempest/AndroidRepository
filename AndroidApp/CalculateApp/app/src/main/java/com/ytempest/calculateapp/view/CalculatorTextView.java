package com.ytempest.calculateapp.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;


/**
 * @author ytempest
 *         Description：该类是一个用于显示计算器表达式内容的View，主要负责表达式内容的添加、删除和显示，
 *         内容的合理性检测以及修复都交给 InputHelper类进行处理。
 */
public class CalculatorTextView extends TextView {
    private static final String TAG = "CalculatorTextView";

    private static final int CHANGE_TEXT_SIZE_THRESHOLD = 15;

    private String mPreExpression = "";
    private String mCurExpression = "0";
    private float mShrinkSize;
    private float mOriginSize;
    private int mBracketCount;


    public CalculatorTextView(Context context) {
        this(context, null);
    }

    public CalculatorTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalculatorTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initConfigure();
    }

    private void initConfigure() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        mShrinkSize = width / 16;
        mOriginSize = width / 12;
        setTextSize(TypedValue.COMPLEX_UNIT_PX, mOriginSize);
        setText(formatExpression(mPreExpression, mCurExpression));
    }

    public void deleteLastChar() {
        if (mCurExpression.equals("0")) {
            return;
        }
        // 如果表达式只有一个字符，那么直接置为0
        if (mCurExpression.length() == 1) {
            mCurExpression = "0";
        } else {
            mCurExpression = mCurExpression.substring(0, mCurExpression.length() - 1);
            // 根据表达式长度重设字体大小
            restoreTextSize(mCurExpression);
            // 判断清除的字符是否为左右括号并更新括号数量
            updateBracketCount(String.valueOf(mCurExpression.charAt(mCurExpression.length() - 1)));
        }

        setText(formatExpression(mPreExpression, mCurExpression));
    }

    /**
     * 判断字符ch是否可以添加到表达式的末尾，可以则添加，否则return
     */
    public void addToExp(String ch) {

        // 判断输入字符的合理性，如果不合理则return
        if (!InputHelper.isInputCorrect(ch, mCurExpression)) {
            return;
        }

        // 判断是否可以添加左右括号
        if (!canAddBracket(ch)) {
            return;
        }

        // 根据要添加的字符和表达式的内容进行适应性修改
        mCurExpression = InputHelper.autoRepairExpression(mCurExpression, ch);

        mCurExpression += ch;
        // 根据当前表达式的长度改变字体大小
        shrinkTextSize(mCurExpression);
        setText(formatExpression(mPreExpression, mCurExpression));
    }


    /**
     * 判断表达式的长度是否等于15，如果是则将字体大小恢复到默认情况
     */
    private void restoreTextSize(String expression) {
        if (expression.length() == CHANGE_TEXT_SIZE_THRESHOLD) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mOriginSize);
        }
    }

    /**
     * 判断表达式的长度是否已经超过15，如果超过则缩小字体大小
     */
    private void shrinkTextSize(String expression) {
        if (expression.length() == CHANGE_TEXT_SIZE_THRESHOLD + 1) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mShrinkSize);
        }
    }

    /**
     * 判断表达式是否合理，即是否是一个可以计算的表达式
     */
    public boolean isExpressionCorrect() {
        // 如果表达式是以数字或者右括号结尾则提交
        return mBracketCount == 0 && InputHelper.isMatch("\\d|\\)", String.valueOf(mCurExpression.charAt(mCurExpression.length() - 1)));
    }

    public void setResult(String result) {
        // 根据表达式结果的长度动态更改文字的大小
        updateTextSize(result);

        // 生成历史计算记录
        mPreExpression = generatePreCalculateRecord(mCurExpression, result);

        // 默认当前表达式为上一个表达式的计算结果
        mCurExpression = result;

        setText(formatExpression(mPreExpression, mCurExpression));
    }

    /**
     * 根据计算结果的长度动态更新字体大小
     */
    private void updateTextSize(String result) {
        int length = result.length();
        if (length <= CHANGE_TEXT_SIZE_THRESHOLD) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mOriginSize);
        } else if (length > CHANGE_TEXT_SIZE_THRESHOLD) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mShrinkSize);
        }
    }


    /**
     * 生成历史的计算记录
     *
     * @param exp    计算的表达式
     * @param result 计算的结果
     */
    private String generatePreCalculateRecord(String exp, String result) {
        return "<small><font color=#686868>" + exp + "<br/>=" + result + "</font></small><br/>- - - - - - - - - - - - - -<br/><br/>";
    }

    /**
     * 格式化上一个表达式和当前的表达式
     */
    private Spanned formatExpression(String preExp, String curExp) {
        return Html.fromHtml(preExp + "<big><font color=#4A4A4A>" + curExp + "</font></big>");
    }


    public String getCurrentExpression() {
        return mCurExpression;
    }

    private boolean canAddBracket(String ch) {
        if ("(".equals(ch)) {
            mBracketCount++;
        } else if (")".equals(ch)) {
            if (mBracketCount == 0) {
                return false;
            }
            mBracketCount--;
        }
        return true;
    }

    /**
     * 用于在清除括号时对括号数量进行恢复
     */
    private void updateBracketCount(String ch) {
        if ("(".equals(ch)) {
            mBracketCount--;
        } else if (")".equals(ch)) {
            mBracketCount++;
        }
    }


    /**
     * 复位
     */
    public void resetExpression() {
        // 将表达式置"0"
        mCurExpression = "0";
        // 将括号的数量置0
        mBracketCount = 0;
        // 恢复默认字体大小
        setTextSize(TypedValue.COMPLEX_UNIT_PX, mOriginSize);
        setText(formatExpression(mPreExpression, mCurExpression));
    }
}
