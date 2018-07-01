package com.ytempest.widget.binnerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ytempest.widget.R;


/**
 * @author ytempest
 *         Description：
 */
public class BannerView extends RelativeLayout {
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
        mDotContainerView = (LinearLayout) findViewById(R.id.ll_dot_container);
        mBannerBottomView = findViewById(R.id.rl_banner_bottom_view);
        mBannerBottomView.setBackgroundColor(mBottomColor);
    }

    /**
     * 设置 BannerViewPager 适配器 并初始化轮播器
     */
    public void setAdapter(BannerAdapter adapter) {
        if (mBannerAdapter != null) {
            throw new IllegalArgumentException("you had set up the adapter, please set up again");
        }

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

        // 监听 BannerViewPager 的滑动以处理页面切换和用户触摸页面时暂停轮播
        mBannerViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // 切换页面
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

        // 如果用户没有设置宽高比例，那么么就return
        if (mHeightProportion == 0 || mWidthProportion == 0) {
            return;
        }

        // 为什么要开线程？因为在初始化BannerView的时候是获取不到View的高度的，只能通过
        // post一个Runnable去获取宽高，当执行这个run()方法的时候，View已经初始化完毕了
        // 根据用户指定的宽高比例动态指定高度
        post(new Runnable() {
            @Override
            public void run() {
                // 获取BannerView的宽度
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

        // 添加相应数量的指示点
        for (int i = 0; i < count; i++) {
            DotView dotView = new DotView(mContext);
            // 设置大小
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mDotSize, mDotSize);
            // 设置左右间距
            params.leftMargin = params.rightMargin = mDotDistance;
            dotView.setLayoutParams(params);

            if (i == 0) {
                // 设置默认选中的指示点
                dotView.setImageDrawable(mIndicatorFocusDrawable);
            } else {
                dotView.setImageDrawable(mIndicatorNormalDrawable);
            }
            mDotContainerView.addView(dotView);
        }
    }

    /**
     * 初始化底部文字
     */
    private void initBannerText() {
        mBannerTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTextSize);
        mBannerTextView.setTextColor(mTextColor);
        // 初始化第一个页面的文字描述
        String firstDesc = mBannerAdapter.getBannerText(0);
        mBannerTextView.setText(firstDesc);
    }

    /**
     * 切换到新的页面
     *
     * @param position 新的页面的位置
     */
    private void pageSelect(int position) {
        // 1、把之前亮着的指示点 设置为默认
        DotView oldDotView = (DotView)
                mDotContainerView.getChildAt(mCurrentPosition);
        oldDotView.setImageDrawable(mIndicatorNormalDrawable);

        // 2、把新位置的指示点 点亮  position：0 --> 2的31次方
        int newPosition = position % mBannerAdapter.getCount();
        DotView currentDotView = (DotView)
                mDotContainerView.getChildAt(newPosition);
        currentDotView.setImageDrawable(mIndicatorFocusDrawable);


        // 3、设置新页面的文字描述
        String bannerDesc = mBannerAdapter.getBannerText(newPosition);
        mBannerTextView.setText(bannerDesc);

        // 4、记录当前位置
        mCurrentPosition = newPosition;
    }

    /**
     * 设置指示点和文字描述的位置
     */
    private void initDotIndicatorGravity() {
        int dotGravity = getDotGravity();
        mDotContainerView.setGravity(dotGravity);

        // 下面开始设置页面描述文字的位置

        // 如果指示点在中间，那么文字会默认设置在左边
        if (dotGravity == Gravity.CENTER) {
            return;
        }

        // 根据指示点的位置反向设置文字，指示点在左，那么文字就会在右
        LayoutParams params = (LayoutParams) mBannerTextView.getLayoutParams();
        int textViewGravity;
        if (dotGravity == Gravity.START) {
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
    private int getDotGravity() {
        switch (mDotGravity) {
            case 0:
                return Gravity.CENTER;
            case -1:
                return Gravity.START;
            case 1:
                return Gravity.END;
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
        mDotContainerView.setVisibility(View.GONE);
    }

    /**
     * 显示页面指示器
     */
    public void showPageIndicator() {
        mDotContainerView.setVisibility(View.VISIBLE);
    }
}
