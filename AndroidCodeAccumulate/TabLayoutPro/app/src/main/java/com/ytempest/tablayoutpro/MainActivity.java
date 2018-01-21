package com.ytempest.tablayoutpro;


import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author ytempest
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TabLayoutPro mTabLayout;

    private ViewPager mViewPager;

    private String[] mItems = {"one", "Two", "three", "four", "five", "six"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initViewData();
    }

    private void initView() {
        mTabLayout = (TabLayoutPro) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
    }


    protected void initViewData() {
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ItemFragment.newInstance(mItems[position]);
            }

            @Override
            public int getCount() {
                return mItems.length;
            }
        };
        // 设置 ViewPager的适配器
        mViewPager.setAdapter(adapter);

        // 设置 TabLayout和ViewPager联动
        mTabLayout.setupWithViewPager(mViewPager);

/*        // 使用默认的 TabLayout 条目
        for (int i = 0; i < mItems.length; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setIcon(R.mipmap.ic_launcher);
            tab.setText(mItems[i]);

        }*/

        // 使用自定义View实现 TabLayout的条目
        for (int i = 0; i < mItems.length; i++) {
            //获得每一个tab
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                //给每一个tab设置自定义view
                tab.setCustomView(R.layout.tab_item);
                if (i == 0) {
                    // 设置第一个tab的TextView是被选择的样式
                    setSelectedTab(tab, Color.RED, true);
                }
                //设置tab上的文字
                TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tab_text);
                textView.setText(mItems[i]);
            }
        }


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            /**
             * 当滑动到新的tab 或者 点击新的tab，这个方法会被调用
             * @param tab 新的 tab
             */
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setSelectedTab(tab, Color.RED, true);
                mViewPager.setCurrentItem(tab.getPosition());

            }

            /**
             * 如果当前的tab失去选择，这个方法会被调用
             * @param tab 失去选择的 tab
             */
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setSelectedTab(tab, Color.BLACK, false);
            }

            /**
             * 如果当前的tab已经被选择，再次点击会调用该方法
             * @param tab 当前的 tab
             */
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Toast.makeText(MainActivity.this, "you click the " + mItems[tab.getPosition()], Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 设置 tab 的状态
     *
     * @param tab      要设置的tab
     * @param color    设置的颜色
     * @param selected 是否设置选择
     */
    private void setSelectedTab(TabLayout.Tab tab, int color, boolean selected) {
        TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tab_text);
        textView.setTextColor(color);
        textView.setSelected(selected);
    }
}


