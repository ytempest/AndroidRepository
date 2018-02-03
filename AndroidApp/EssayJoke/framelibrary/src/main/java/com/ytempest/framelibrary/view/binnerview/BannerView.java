package com.ytempest.framelibrary.view.binnerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ytempest.framelibrary.R;


/**
 * @author ytempest
 *         Description：
 */
public class BannerView extends RelativeLayout {
    private static final String TAG = "BannerView";
    /**
     * 轮播的ViewPager
     */
    private BannerViewPager mBannerViewPager;
    /**
     * 轮播的描述
     */
    private TextView mBannerTextView;
    /**
     * 指示点的容器
     */
    private LinearLayout mDotContainerView;
    /**
     * 自定义的BannerAdapter
     */
    private BannerAdapter mBannerAdapter;

    private Context mContext;

    /**
     * 指示点选中的Drawable
     */
    private Drawable mIndicatorFocusDrawable;
    /**
     * 指示点默认的Drawable
     */
    private Drawable mIndicatorNormalDrawable;

    private int mCurrentPosition = 0;

    /**
     * 点的显示位置  默认右边
     */
    private int mDotGravity = 1;
    /**
     * 指示点的大小  默认8dp
     */
    private int mDotSize = 8;
    /**
     * 指示点之间的间距  默认4dp
     */
    private int mDotDistance = 4;
    /**
     * 底部容器
     */
    private View mBannerBottomView;
    /**
     * 部容器颜色默认透明
     */
    private int mBottomColor = Color.TRANSPARENT;
    /**
     * 宽高比例
     */
    private float mWidthProportion = 0;
    private float mHeightProportion = 0;
    /**
     * 底部文字颜色
     */
    private int mTextColor = Color.BLACK;
    /**
     * 底部文字大小，默认13sp
     */
    private int mTextSize = 13;
    /**
     * 标志ViewPager的滑动是用户滑动而不是自动滑动
     */
    private boolean isUserTouch = false;


    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        // 把布局加载到这个View里面
        inflate(context, R.layout.ui_banner_layout, this);

        initAttribute(context, attrs);

        initView();
    }

    /**
     * 初始化自定义属性
     */
    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BannerView);

        // 获取点的位置
        mDotGravity = array.getInt(R.styleable.BannerView_dotGravity, mDotGravity);
        // 获取点的颜色（默认、选中）
        mIndicatorFocusDrawable = array.getDrawable(R.styleable.BannerView_dotSelectColor);
        if (mIndicatorFocusDrawable == null) {
            // 如果在布局文件中没有配置点的颜色  有一个默认值
            mIndicatorFocusDrawable = new ColorDrawable(Color.RED);
        }
        mIndicatorNormalDrawable = array.getDrawable(R.styleable.BannerView_dotNormalColor);
        if (mIndicatorNormalDrawable == null) {
            // 如果在布局文件中没有配置点的颜色  有一个默认值
            mIndicatorNormalDrawable = new ColorDrawable(Color.WHITE);
        }
        // 获取点的大小和距离
        mDotSize = (int) array.getDimension(R.styleable.BannerView_dotSize, dipToPx(mDotSize));
        mDotDistance = (int) array.getDimension(R.styleable.BannerView_dotDistance, dipToPx(mDotDistance));

        // 获取底部的颜色
        mBottomColor = array.getColor(R.styleable.BannerView_bottomColor, mBottomColor);

        // 获取宽高比例
        mWidthProportion = array.getFloat(R.styleable.BannerView_widthProportion, mWidthProportion);
        mHeightProportion = array.getFloat(R.styleable.BannerView_heightProportion, mHeightProportion);

        // 获取 底部文字属性
        mTextColor = array.getColor(R.styleable.BannerView_bannerTextColor, mTextColor);
        mTextSize = (int) array.getDimension(R.styleable.BannerView_bannerTextSize, spToPx(mTextSize));

        array.recycle();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mBannerViewPager = (BannerViewPager) findViewById(R.id.banner_view_pager);
        mBannerTextView = (TextView) findViewById(R.id.tv_banner_text);
        mDotContainerView = (LinearLayout) findViewById(R.id.dot_container);
        mBannerBottomView = findViewById(R.id.banner_bottom_view);
        mBannerBottomView.setBackgroundColor(mBottomColor);
    }

    /**
     * 设置 BannerViewPager 适配器 并初始化轮播器
     */
    public void setAdapter(BannerAdapter adapter) {
        mBannerAdapter = adapter;

        // 初始化 轮播器
        initBannerView();


    }

    private void initBannerView() {
        // 初始化指示点
        initDotIndicator();

        // 初始化底部文字
        initBannerText();

        mBannerViewPager.setBannerAdapter(mBannerAdapter);

        // 监听 BannerViewPager 的滑动
        mBannerViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // 改变指示点的状态
                pageSelect(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    // 只有用户滑动 ViewPager，才会满足 SCROLL_STATE_DRAGGING这个条件
                    mBannerViewPager.pauseRoll();
                    // 标志用户滑动了BannerViewPager
                    isUserTouch = true;
                } else if (state == ViewPager.SCROLL_STATE_IDLE && isUserTouch) {
                    // 由于自动轮播的时候也会满足 SCROLL_STATE_IDLE 这一个
                    // 条件，所以增加一个是否用户点击的条件
                    mBannerViewPager.resumeRoll();
                    isUserTouch = false;
                }
            }
        });

        // 设置 BannerViewPager 的位置以确保能无限轮播
        mBannerViewPager.setCurrentItem(0x6C413B80);

        // 自适应高度 动态指定高度
        if (mHeightProportion == 0 || mWidthProportion == 0) {
            return;
        }
        // 为什么要开线程
        post(new Runnable() {
            @Override
            public void run() {
                // 动态指定宽高  计算高度
                int width = getMeasuredWidth();
                // 计算高度
                int height = (int) (width * mHeightProportion / mWidthProportion);
                // 指定宽高
                getLayoutParams().height = height;
                mBannerViewPager.getLayoutParams().height = height;
            }
        });
    }

    /**
     * 初始化点的指示器
     */
    private void initDotIndicator() {
        // 设置指示点的位置，如果指示点在左边，那就要设置文字描述在右边
        initDotIndicatorGravity();

        // 获取广告的数量
        int count = mBannerAdapter.getCount();

        mDotContainerView.removeAllViews();

        for (int i = 0; i < count; i++) {
            // 不断的往点的指示器添加圆点
            DotIndicatorView indicatorView = new DotIndicatorView(mContext);
            // 设置大小
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mDotSize, mDotSize);
            // 设置左右间距
            params.leftMargin = params.rightMargin = mDotDistance;
            indicatorView.setLayoutParams(params);

            if (i == 0) {
                // 指示点选中的位置
                indicatorView.setImageDrawable(mIndicatorFocusDrawable);
            } else {
                // 指示点未选中的位置
                indicatorView.setImageDrawable(mIndicatorNormalDrawable);
            }
            mDotContainerView.addView(indicatorView);
        }
    }

    /**
     * 初始化底部文字
     */
    private void initBannerText() {
        Log.e(TAG, "initBannerText: mBannerTextView.getTextSize() --> " + mBannerTextView.getTextSize());
        Log.e(TAG, "initBannerText: mTextSize --> " + mTextSize);

        mBannerTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTextSize);
        mBannerTextView.setTextColor(mTextColor);
        // 初始化第一个页面的文字描述
        String firstDesc = mBannerAdapter.getBannerText(0);
        mBannerTextView.setText(firstDesc);
    }

    /**
     * 页面切换的回调
     *
     * @param position 新的页面的位置
     */
    private void pageSelect(int position) {
        // 1、把之前亮着的指示点 设置为默认
        DotIndicatorView oldIndicatorView = (DotIndicatorView)
                mDotContainerView.getChildAt(mCurrentPosition);
        oldIndicatorView.setImageDrawable(mIndicatorNormalDrawable);

        // 2、把新位置的指示点 点亮  position：0 --> 2的31次方
        mCurrentPosition = position % mBannerAdapter.getCount();
        DotIndicatorView currentIndicatorView = (DotIndicatorView)
                mDotContainerView.getChildAt(mCurrentPosition);
        currentIndicatorView.setImageDrawable(mIndicatorFocusDrawable);

        // 设置文字描述
        String bannerDesc = mBannerAdapter.getBannerText(mCurrentPosition);
        mBannerTextView.setText(bannerDesc);
    }

    /**
     * 设置指示点和文字描述的位置
     */
    private void initDotIndicatorGravity() {
        int gravity = getDotGravity();
        mDotContainerView.setGravity(gravity);
        if (gravity == Gravity.CENTER) {
            return;
        }
        LayoutParams params = (LayoutParams) mBannerTextView.getLayoutParams();
        int textViewGravity;
        if (gravity == Gravity.LEFT) {
            textViewGravity = RelativeLayout.ALIGN_PARENT_RIGHT;
        } else {
            textViewGravity = RelativeLayout.ALIGN_PARENT_LEFT;
        }
        params.addRule(textViewGravity, RelativeLayout.TRUE);
        mBannerTextView.setLayoutParams(params);
    }

    /**
     * 把 dip 转成 px
     */
    private int dipToPx(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dip, getResources().getDisplayMetrics());
    }

    /**
     * 把 sp 转成 px
     */
    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getResources().getDisplayMetrics());
    }

    /**
     * 开始滚动 BannerViewPager
     */
    public void startRoll() {
        mBannerViewPager.startRoll();
    }


    /**
     * 获取指示点的显示的位置
     */
    public int getDotGravity() {
        switch (mDotGravity) {
            case 0:
                return Gravity.CENTER;
            case -1:
                return Gravity.LEFT;
            case 1:
                return Gravity.RIGHT;
            default:
                break;
        }
        return Gravity.CENTER;
    }

    /**
     * 设置 BannerViewPager 页面的点击回调监听
     */
    public void setOnBannerItemClickListener(BannerViewPager.BannerItemClickListener listener) {
        mBannerViewPager.setOnBannerItemClickListener(listener);
    }


    /**
     * 隐藏页面指示器
     */
    public void hidePageIndicator() {
        mDotContainerView.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示页面指示器
     */
    public void showPageIndicator() {
        mDotContainerView.setVisibility(View.VISIBLE);
    }
}
