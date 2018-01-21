package com.ytempest.test2;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ytempest.baselibrary.base.BaseActivity;
import com.ytempest.baselibrary.dialog.AlertDialog;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.ioc.ViewUtils;
import com.ytempest.framelibrary.db.DaoSupportFactory;
import com.ytempest.framelibrary.db.IDaoSupport;
import com.ytempest.framelibrary.navigation.DefaultNavigationBar;
import com.ytempest.test2.mode.Test;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends BaseActivity {

    private static final String TAG = "SecondActivity";

    @ViewById(R.id.bt_dialog)
    private Button mBtDialog;
    private IDaoSupport daoSupport = DaoSupportFactory.getFactory().getDao(Test.class);
    @ViewById(R.id.bt_add)
    private Button mBtAdd;
    @ViewById(R.id.bt_delete)
    private Button mBtDelete;
    @ViewById(R.id.bt_update)
    private Button mBtUpdate;
    @ViewById(R.id.bt_select)
    private Button mBtSelect;


    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar = new DefaultNavigationBar.Builder(this)
                .setTitle("投稿")
                .setRightText("发表")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .build();


        ViewUtils.inject(this);
    }

    @Override
    protected void initView() {

    }

    /**
     * 获取布局layout的Id
     *
     * @return 布局Id
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_second;
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.bt_dialog)
    private void btDialogClick(Button btDialog) {
        AlertDialog dialog = new AlertDialog.Builder(SecondActivity.this)
                .setContentView(R.layout.detail_comment_dialog)
                .fullWidth()
                .formBottom(true)
                .show();

        final EditText editText = dialog.getView(R.id.et_comment_editor);

        dialog.setOnClickListener(R.id.bt_submit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastShort("发送" + editText.getText());
            }
        });
        dialog.setOnClickListener(R.id.iv_share_weibo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastShort("分享到新浪微博");
            }
        });
        dialog.setOnClickListener(R.id.iv_share_wechat, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastShort("分享到微信");
            }
        });
    }




    @OnClick(R.id.bt_add)
    private void btAddClick(Button btAdd) {
        List<Test> lists = new ArrayList<Test>();
        for (int i = 1; i < 11; i++) {
            lists.add(new Test(i, (char) (65 + i) + "", "Student"));
        }
        daoSupport.insert(lists);
    }

    @OnClick(R.id.bt_delete)
    private void btDeleteClick(Button btDelete) {
        daoSupport.delete("error_no<?", "5");
    }

    @OnClick(R.id.bt_update)
    private void btUpdateClick(Button btUpdate) {
        Test test = new Test(100, "GG", "Teacher");
//        daoSupport.update(test, "error_no>2 and error_no<6", null);
        daoSupport.update(test, "error_no>? and error_no<?", new String[]{"2", "6"});
    }

    @OnClick(R.id.bt_select)
    private void btSelectClick(Button btSelect) {
        List<Test> lists = daoSupport.getQuerySupport().queryAll();
        for (Test person : lists) {
            Log.e(TAG, "" + person.getError_no());
            Log.e(TAG, "" + person.getError_code());
            Log.e(TAG, "" + person.getError_desc() + "\n\n");
        }
    }
}