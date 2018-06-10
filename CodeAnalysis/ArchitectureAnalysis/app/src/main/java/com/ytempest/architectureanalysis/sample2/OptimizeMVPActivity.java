package com.ytempest.architectureanalysis.sample2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ytempest.architectureanalysis.R;
import com.ytempest.architectureanalysis.retrofit.UserInfoResult;
import com.ytempest.architectureanalysis.sample2.base.BaseMVPActivity;
import com.ytempest.architectureanalysis.sample2.inject.InjectPresenter;
import com.ytempest.architectureanalysis.sample2.mvp.UserInfoContract;
import com.ytempest.architectureanalysis.sample2.mvp.UserInfoPresenter;

public class OptimizeMVPActivity extends BaseMVPActivity implements UserInfoContract.UserInfoView {

    private static final String TAG = "OptimizeMVPActivity";

    @InjectPresenter
    private UserInfoPresenter mPresenter;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mvp);

        mTextView = findViewById(R.id.text);

        mPresenter.getUser("dy", "123");
    }

    @Override
    public void onLoading() {
        mTextView.setText("loading");
    }

    @Override
    public void onError(Throwable throwable) {
        mTextView.setText(throwable.toString());
    }

    @Override
    public void onSucceed(UserInfoResult result) {
        mTextView.setText(result.getData().toString());
    }

}
