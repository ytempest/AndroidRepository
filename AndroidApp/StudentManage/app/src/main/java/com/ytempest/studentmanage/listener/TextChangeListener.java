package com.ytempest.studentmanage.listener;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author ytempest
 *         Description：用于监听EditText的内容是否发生过变化，如果发生过则通过回调函数通知调用者
 */
public abstract class TextChangeListener implements TextWatcher {

    private String originalText;

    public TextChangeListener(EditText editText) {
        originalText = editText.getText().toString();

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        onTextChanged(!originalText.equals(s.toString()), String.valueOf(s));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    public abstract void onTextChanged(boolean hasChanged,String text);
}
