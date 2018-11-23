package com.ytempest.calculateapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ytempest.calculateapp.util.CalculateUtils;
import com.ytempest.calculateapp.util.StatusBarUtils;
import com.ytempest.calculateapp.view.CalculatorTextView;

/**
 * @author ytempest
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private CalculatorTextView mResultTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 修改状态栏颜色
        StatusBarUtils.statusBarTintColor(this, Color.parseColor("#C9C9C9"));

        initView();

        initClickListener();

    }

    private void initView() {
        mResultTv = findViewById(R.id.tv_result);
    }


    /**
     * 初始化按钮的点击事件
     */
    private void initClickListener() {
        // 获取装载了按钮的顶层LinearLayout
        LinearLayout linearLayout = findViewById(R.id.ll_button_container);
        setButtonClickListener(linearLayout);
    }

    /**
     * 递归为每一个按钮设置点击事件
     */
    public void setButtonClickListener(View view) {
        if (view instanceof ViewGroup) {
            // 如果是ViewGroup则递归设置子View的点击事件
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setButtonClickListener(viewGroup.getChildAt(i));
            }
        } else {
            // 如果是View就设置点击事件
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 复位按钮
            case R.id.bt_reset:
                mResultTv.resetExpression();
                break;

            // 清除按钮
            case R.id.bt_clear:
                mResultTv.deleteLastChar();
                break;

            // 等于按钮
            case R.id.bt_equal:
                // 如果表达式是以数字或者右括号结尾则提交
                if (mResultTv.isExpressionCorrect()) {
                    String expression = mResultTv.getCurrentExpression();
                    expression = expression.replace('×', '*');
                    expression = expression.replace('÷', '/');
                    if (expression.startsWith("-")) {
                        expression = "0" + expression;
                    }
                    mResultTv.setResult(String.valueOf(CalculateUtils.calculateInfixExpression(expression)));
                }
                break;

            // 其他所有按钮
            default:
                // 获取用户点击的字符
                String ch = (String) ((TextView) view).getText();
                mResultTv.addToExp(ch);
                break;

        }

    }
}
