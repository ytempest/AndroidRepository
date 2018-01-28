package com.ytempest.framelibrary.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.VectorEnabledTintResources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ytempest.baselibrary.base.BaseActivity;
import com.ytempest.framelibrary.skin.SkinManager;
import com.ytempest.framelibrary.skin.SkinResource;
import com.ytempest.framelibrary.skin.attr.SkinAttr;
import com.ytempest.framelibrary.skin.attr.SkinView;
import com.ytempest.framelibrary.skin.callback.ISkinChangeListener;
import com.ytempest.framelibrary.skin.support.SkinAppCompatViewInflater;
import com.ytempest.framelibrary.skin.support.SkinAttrSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description: 兼容换肤
 */
public abstract class BaseSkinActivity extends BaseActivity implements LayoutInflaterFactory, ISkinChangeListener {

    private static final String TAG = "BaseSkinActivity";

    /**
     * 自己的 View 加载类
     */
    private SkinAppCompatViewInflater mAppCompatViewInflater;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 获取系统的 LayoutInflater 对象
        LayoutInflater defaultLayoutInflater = LayoutInflater.from(this);
        // 设置拦截 View 创建的 mFactory
        LayoutInflaterCompat.setFactory(defaultLayoutInflater, this);

        super.onCreate(savedInstanceState);
    }

    /**
     * 这个方法会在使用系统方法创建每一个View前调用
     * 在这里对拦截到的 View 进行修改
     *
     * @param parent  View的父类
     * @param name    View的名称
     * @param context 上下文
     * @param attrs   View的属性集
     * @return
     */
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {

        // 1. 让V7包创建兼容的 View
        View view = createView(parent, name, context, attrs);

        if (view != null) {
            // 2. 从拦截到的 View 的属性集合中获取需要换肤的属性集合
            List<SkinAttr> skinAttrs = SkinAttrSupport.getSkinAttrs(context, attrs);

            // 3. 生成换肤后的 SkinView
            SkinView skinView = new SkinView(view, skinAttrs);

            // 4. 统一交给 SkinManager 管理
            managerSkinView(skinView);

            // 5. 判断要不要换肤
            SkinManager.getInstance().checkSkinStatus(skinView);

        }
        return view;
    }

    /**
     * 将 SkinView添加到 SkinManager中需要换肤的View集合中，由 SkinManager统一管理
     *
     * @param skinView
     */
    private void managerSkinView(SkinView skinView) {
        // 获取当前的 this 指向的 Activity下的所有需要换肤的View
        List<SkinView> skinViews = SkinManager.getInstance().getSkinViews(this);
        if (skinViews == null) {
            // 如果该 Activity下需要换肤的View还没有缓存，先缓存
            skinViews = new ArrayList<>();
            SkinManager.getInstance().register(this, skinViews);
        }
        // 将该 SkinView 添加到换肤集合中
        skinViews.add(skinView);
    }


    /**
     * 创建V7包兼容的View
     *
     * @param parent
     * @param name
     * @param context
     * @param attrs
     * @return
     */
    public View createView(View parent, final String name, @NonNull Context context,
                           @NonNull AttributeSet attrs) {

        final boolean isPre21 = Build.VERSION.SDK_INT < 21;
        if (mAppCompatViewInflater == null) {
            mAppCompatViewInflater = new SkinAppCompatViewInflater();
        }

        final boolean inheritContext = isPre21 && shouldInheritContext((ViewGroup) parent);

        return mAppCompatViewInflater.createView(parent, name, context, attrs, inheritContext,
                isPre21,
                true,
                VectorEnabledTintResources.shouldBeUsed());
    }

    private boolean shouldInheritContext(ViewParent parent) {
        if (parent == null) {
            // 如果继承的父类 View为空
            return false;
        }
        final View windowDecor = getWindow().getDecorView();
        while (true) {
            if (parent == null) {
                return true;
            } else if (parent == windowDecor || !(parent instanceof View)
                    || ViewCompat.isAttachedToWindow((View) parent)) {
                return false;
            }
            parent = parent.getParent();
        }
    }

    @Override
    public void changeSkin(SkinResource skinResource) {
    }

    @Override
    protected void onDestroy() {
        // 在 activity 被回收的时候释放单例保存的 activity的引用，防止内存泄漏
        SkinManager.getInstance().unregister(this);
        super.onDestroy();
    }
}
