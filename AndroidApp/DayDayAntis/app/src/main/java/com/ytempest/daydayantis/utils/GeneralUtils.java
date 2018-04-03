package com.ytempest.daydayantis.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.widget.EditText;

import com.ytempest.daydayantis.R;

/**
 * @author ytempest
 *         Description：
 */
public class GeneralUtils {

    /**
     * 防止创建对象
     */
    private GeneralUtils() {
    }

    /**
     * 判断是否是合格的手机号码
     */
    public static boolean judgePhoneQual(EditText phoneEt) {
        return judgePhoneQual(phoneEt.getText().toString().trim());
    }

    /**
     * 判断是否是合格的手机号码
     */
    public static boolean judgePhoneQual(String number) {
        return number.trim().matches("^1[34568]\\d{9}$");
    }

    /**
     * 判断是否是一个 mailbox
     */
    public static boolean judgeEmailQual(EditText emailEt) {
        return judgeEmailQual(emailEt.getText().toString().trim());
    }

    /**
     * 判断是否是一个 mailbox
     */
    public static boolean judgeEmailQual(String email) {
        return email
                .matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
    }

    /**
     * 根据颜色值获取16进制的颜色值，不包括透明度
     */
    public static String getColorValue(Context context, int colorId) {
        StringBuilder stringBuilder = new StringBuilder();
        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = context.getResources().getColor(colorId, null);
        } else {
            color = context.getResources().getColor(colorId);
        }
        stringBuilder.append("#");
//        stringBuilder.append(Integer.toHexString(Color.alpha(color)));
        stringBuilder.append(Integer.toHexString(Color.red(color)));
        stringBuilder.append(Integer.toHexString(Color.green(color)));
        stringBuilder.append(Integer.toHexString(Color.blue(color)));
        return stringBuilder.toString();
    }

    /**
     * 将EditText的光标移到文本最后
     */
    public static void setCursorLast(EditText editText) {
        // 把光标移动到最后
        Editable editable = editText.getText();
        Selection.setSelection(editable, editable.length());
    }

}
