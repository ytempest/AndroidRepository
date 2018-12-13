package com.ytempest.studentmanage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.ytempest.baselibrary.view.recyclerview.LoadRecyclerView;
import com.ytempest.baselibrary.view.recyclerview.LoadViewCreator;
import com.ytempest.studentmanage.R;

/**
 * @author ytempest
 *         Description：
 */
public class DefaultLoadViewCreator implements LoadViewCreator {
    private TextView mLoadTv;
    private View mRefreshIv;

    @Override
    public View getLoadView(Context context, ViewGroup parent) {
        View refreshView = LayoutInflater.from(context).inflate(R.layout.layout_load_footer_view, parent, false);
        mLoadTv = (TextView) refreshView.findViewById(R.id.tv_load);
        mRefreshIv = refreshView.findViewById(R.id.iv_refresh);
        return refreshView;
    }

    @Override
    public void onPull(int currentDragHeight, int refreshViewHeight, int currentRefreshStatus) {
        if (currentRefreshStatus == LoadRecyclerView.LOAD_STATUS_ON_PULL) {
            mLoadTv.setText("上拉加载更多");
        }
        if (currentRefreshStatus == LoadRecyclerView.LOAD_STATUS_LOOSE_PULL) {
            mLoadTv.setText("松开加载更多");
        }
    }

    @Override
    public void onLoading() {
        mLoadTv.setVisibility(View.INVISIBLE);
        mRefreshIv.setVisibility(View.VISIBLE);

        // 加载的时候不断旋转
        RotateAnimation animation = new RotateAnimation(0, 720,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(1000);
        mRefreshIv.startAnimation(animation);
    }

    @Override
    public void onStopLoad() {
        // 停止加载的时候清除动画
        mRefreshIv.setRotation(0);
        mRefreshIv.clearAnimation();
        mLoadTv.setText("上拉加载更多");
        mLoadTv.setVisibility(View.VISIBLE);
        mRefreshIv.setVisibility(View.INVISIBLE);
    }
}
