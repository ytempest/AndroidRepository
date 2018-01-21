package com.ytempest.test2;


import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ytempest.baselibrary.indicator.IndicatorAdapter;
import com.ytempest.baselibrary.indicator.TrackIndicatorView;
import com.ytempest.baselibrary.ioc.ViewUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class MainIndicatorActivity extends BaseSkinActivity {
    private static final String TAG = "TestActivity";

    private TrackIndicatorView mTrackIndicatorView;
    private String[] items = {"直播1", "推荐2", "视KK频3", "消息4", "用户5", "中心6", "视频7", "消息8", "用户9", "中心10"};
    private List<ColorTrackTextView> mIndicators;
    private ViewPager mViewPager;



    @Override
    protected void initTitle() {
        ViewUtils.inject(this);

    }

    /**
     * 获取布局layout的Id
     *
     * @return 布局Id
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_indicator;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        mTrackIndicatorView = (TrackIndicatorView) findViewById(R.id.indicator_view);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndicators = new ArrayList<ColorTrackTextView>();
        initIndicators();
        initViewPager();
    }

    private void initViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ItemFragment.newInstance(items[position]);
            }

            @Override
            public int getCount() {
                return items.length;
            }
        });



    }

    private void initIndicators() {
        mTrackIndicatorView.setAdapter(new IndicatorAdapter() {
            /**
             * 添加底部跟踪的指示器
             */
            @Override
            public View getBottomTrackView() {
                View view = new View(MainIndicatorActivity.this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                view.setBackgroundColor(Color.GREEN);
                view.setLayoutParams(params);
                return view;
            }

            @Override
            public int getCount() {
                return items.length;
            }

            @Override
            public View getView(int position, ViewGroup parent) {
                TextView textView = new TextView(MainIndicatorActivity.this);
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.GRAY);
                textView.setText(items[position]);
                int padding = 20;
                textView.setPadding(padding, padding, padding, padding);
                return textView;
            }

            /**
             * 重置当前位置
             *
             * @param view
             */
            @Override
            public void restoreIndicator(View view) {
                TextView tv = (TextView) view;
                tv.setTextColor(Color.GRAY);
            }

            /**
             * 高亮当前位置
             *
             * @param view
             */
            @Override
            public void highLightIndicator(View view) {
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
            }
        }, mViewPager);
    }


}


