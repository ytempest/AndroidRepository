package com.ytempest.daydayantis.common;

import android.text.Editable;
import android.text.Selection;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * @author ytempest
 *         Description：用于监听是否显示密码的CheckBox的状态
 */
public class PasswordStatusChangeListener implements CompoundButton.OnCheckedChangeListener {

    private EditText mEtPassword;

    public PasswordStatusChangeListener(EditText editText) {
        this.mEtPassword = editText;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // 显示密码
            mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            // 隐藏密码
            mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        // 把光标移动到最后
        Editable editable = mEtPassword.getText();
        Selection.setSelection(editable, editable.length());
    }
}
