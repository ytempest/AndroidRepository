package com.ytempest.studentmanage.listener;

import android.text.Editable;
import android.text.Selection;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * @author ytempest
 *         Description：
 */
public class PasswordStatusChangeListener implements CompoundButton.OnCheckedChangeListener {

    private final EditText mPwdEditText;

    public PasswordStatusChangeListener(EditText editText) {
        this.mPwdEditText = editText;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // 显示密码
            mPwdEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            // 隐藏密码
            mPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        // 最后把光标移动到末尾
        Editable editable = mPwdEditText.getText();
        Selection.setSelection(editable, editable.length());
    }
}
