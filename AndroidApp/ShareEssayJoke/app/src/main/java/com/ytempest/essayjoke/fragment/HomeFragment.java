package com.ytempest.essayjoke.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.permission.PermissionFail;
import com.ytempest.baselibrary.permission.SmartPermission;
import com.ytempest.baselibrary.permission.PermissionSucceed;
import com.ytempest.baselibrary.util.StatusBarUtils;
import com.ytempest.baselibrary.view.indicator.IndicatorAdapter;
import com.ytempest.baselibrary.view.indicator.TrackIndicatorView;
import com.ytempest.baselibrary.view.indicator.item.ColorTrackTextView;
import com.ytempest.essayjoke.R;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;


/**
 * @author ytempest
 *         Description:
 */
public class HomeFragment extends BaseFragment {

    private static String TAG = "HomeFragment";

    private final static int NUM = 11;

    private String[] items = {"直播", "推荐", "视频", "图片", "段子", "精华", "同城", "游戏"};
    @ViewById(R.id.indicator_view)
    private TrackIndicatorView mIndicatorContainer;
    @ViewById(R.id.view_pager)
    private ViewPager mViewPager;
    @ViewById(R.id.ll_home_fragment_root)
    private LinearLayout mRootView;
    private String[] perms;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        DefaultNavigationBar navigationBar = new DefaultNavigationBar.Builder(mContext, mRootView)
                .setTitle("首页")
                .setRightText("支付测试")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new ShareAction(getActivity())
                                .withText("hello")
                                .addButton("button1","button2","button3","button4")
                                .setDisplayList(
                                        SHARE_MEDIA.SINA,
                                        SHARE_MEDIA.QQ,
                                        SHARE_MEDIA.WEIXIN,
                                        SHARE_MEDIA.ALIPAY,
                                        SHARE_MEDIA.QZONE)
                                .open();


                        /*final AlertDialog dialog = new AlertDialog.Builder(mContext)
                                .setContentView(new PayView(mContext.getApplicationContext()))
                                .fullWidth()
                                .formBottom(true)
                                .setCanceledOnTouchOutside(false)
                                .show();

                        PayView payView = (PayView) dialog.getContentView();
                        payView.setOnClosePayViewListener(new PayView.OnClosePayViewListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });

                        payView.setOnInputFinishListener(new PayView.OnInputFinishListener() {
                            @Override
                            public void onFinish(String password) {
                                Toast.makeText(mContext, "密码：" + password, Toast.LENGTH_SHORT).show();
                            }
                        });*/

                    }
                })
                .hideLeftIcon()
                .build();
        StatusBarUtils.statusBarTintColor(getActivity(), ContextCompat.getColor(getActivity(), R.color.navigation_bar_bg));

    }


    private void requireSomePermission() {
        // 把你想要申请的权限放进这里就行，注意用逗号隔开
        perms = new String[]{

                // 把你想要申请的权限放进这里就行，注意用逗号隔开
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE,
        };
        SmartPermission.with(HomeFragment.this)
                .requestCode(NUM)
                .requestPermission(perms)
                .request();
    }

    @PermissionSucceed(requestCode = NUM)
    private void callPhone() {

        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:10086");
        intent.setData(data);
        startActivity(intent);
        Toast.makeText(mContext, "已经授权", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = NUM)
    private void failPhone() {
        Toast.makeText(mContext, "请授权！！！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.e(TAG, "onRequestPermissionsResult: 授权结果已经回调 -->" + grantResults);
        SmartPermission.onRequestPermissionResult(HomeFragment.this, NUM, grantResults);
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
                ColorTrackTextView textView = new ColorTrackTextView(mContext);
                textView.setTextSize(16);
                textView.setGravity(Gravity.CENTER);
                textView.setText(items[position]);
                textView.setChangeColor(Color.RED);
                int padding = 20;
                textView.setPadding(padding, padding, padding, padding);
                return textView;
            }

            @Override
            public void highLightIndicator(View view, float positionOffset) {
                ColorTrackTextView right = (ColorTrackTextView) view;
                right.setDirection(ColorTrackTextView.Direction.LEFT_TO_RIGHT);
                right.setCurrentProgress(positionOffset);
            }

            @Override
            public void restoreIndicator(View view, float positionOffset) {
                ColorTrackTextView left = (ColorTrackTextView) view;
                left.setDirection(ColorTrackTextView.Direction.RIGHT_TO_LEFT);
                left.setCurrentProgress(1 - positionOffset);
            }

            @Override
            public View getBottomTrackView() {
                View view = new View(mContext);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, 5);
                view.setBackgroundColor(Color.GRAY);
                view.setLayoutParams(params);
                return view;
            }
        }, mViewPager);

    }
}
