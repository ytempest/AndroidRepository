package com.ytempest.test2.colortracktextview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ytempest.baselibrary.indicator.IndicatorAdapter;
import com.ytempest.baselibrary.indicator.TrackIndicatorView;
import com.ytempest.test2.R;


import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private String TAG = "ViewPagerActivity";
    private String[] items = {"直播", "推荐", "视频", "段友秀", "图片", "段子", "精华", "同城", "游戏"};
    private TrackIndicatorView mIndicatorContainer;// 变成通用的
    private List<ColorTrackTextView> mIndicators;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicator);
        mIndicators = new ArrayList<ColorTrackTextView>();
        mIndicatorContainer = (TrackIndicatorView)findViewById(R.id.indicator_view);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        initIndicator();
        initViewPager();
    }


    /**
     * 初始化可变色的指示器
     */
    private void initIndicator() {
        mIndicatorContainer.setAdapter(new IndicatorAdapter() {
            @Override
            public int getCount() {
                return items.length;
            }

            @Override
            public View getView(int position, ViewGroup parent) {
                TextView textView = new TextView(TestActivity.this);
                // 设置颜色
                textView.setTextSize(14);
                textView.setGravity(Gravity.CENTER);
                textView.setText(items[position]);
                textView.setTextColor(Color.BLACK);
                int padding = 20;
                textView.setPadding(padding, padding, padding, padding);
                return textView;
            }

            @Override
            public void highLightIndicator(View view) {
                TextView textView = (TextView) view;
                textView.setTextColor(Color.RED);
            }

            @Override
            public void restoreIndicator(View view) {
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
            }

            @Override
            public View getBottomTrackView() {
                View view = new View(TestActivity.this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, 5);
                view.setBackgroundColor(Color.BLUE);
                view.setLayoutParams(params);
                return view;
            }
        }, mViewPager);
    }

    /**
     * 初始化ViewPager
     */
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

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {

            }
        });

        // 监听ViewPager的滚动
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // 选中毁掉
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


}
