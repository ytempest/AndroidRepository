package com.ytempest.essayjoke.fragment;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.ioc.ViewById;

import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.baselibrary.view.indicator.IndicatorAdapter;
import com.ytempest.baselibrary.view.indicator.TrackIndicatorView;
import com.ytempest.essayjoke.R;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;


/**
 * @author ytempest
 *         Description:
 */
public class HomeFragment extends BaseFragment {

    private String[] items = {"直播", "推荐", "视频", "图片", "段子", "精华","同城","游戏"};
    @ViewById(R.id.indicator_view)
    private TrackIndicatorView mIndicatorContainer;
    @ViewById(R.id.view_pager)
    private ViewPager mViewPager;
    @ViewById(R.id.ll_home_fragment_root)
    private LinearLayout mRootView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        DefaultNavigationBar navigationBar = new DefaultNavigationBar.Builder(context,mRootView)
                .setTitle("首页")
                .setRightText("测试")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setContentView(R.layout.dialog)
                                .fullWidth()
                                .formBottom(true).show();
                    }
                })
                .hideLeftIcon()
                .build();

    }

    @Override
    protected void initData() {
        initIndicator();
        initViewPager();
    }


    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
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
                TextView textView = new TextView(context);
                textView.setTextSize(15);
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
                View view = new View(context);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, 5);
                view.setBackgroundColor(Color.GRAY);
                view.setLayoutParams(params);
                return view;
            }
        }, mViewPager);

    }
}
