package com.ytempest.studentmanage.listener;

import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.widget.EditText;

/**
 * @author ytempest
 *         Description：实现长按EditText将EditText的光标移动到文字末尾
 */
public class LastCharLongClickListener implements View.OnLongClickListener {

    private EditText editText;

    public LastCharLongClickListener(EditText editText) {
        this.editText = editText;
    }

    @Override
    public boolean onLongClick(View v) {
        // 最后把光标移动到末尾
        Editable editable = editText.getText();
        Selection.setSelection(editable, editable.length());
        return true;
    }
}
