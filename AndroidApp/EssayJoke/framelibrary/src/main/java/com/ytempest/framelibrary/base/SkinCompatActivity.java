package com.ytempest.framelibrary.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ytempest.baselibrary.base.BaseActivity;
import com.ytempest.framelibrary.skin.SkinAttrHelper;
import com.ytempest.framelibrary.skin.SkinManager;
import com.ytempest.framelibrary.skin.SkinResource;
import com.ytempest.framelibrary.skin.attr.SkinAttr;
import com.ytempest.framelibrary.skin.attr.SkinView;
import com.ytempest.framelibrary.skin.callback.ISkinChangeProvider;
import com.ytempest.framelibrary.skin.support.SkinAppCompatViewInflater;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description: 拦截当前Activity的所有View，然后把每一个View需要换肤的属性集合保存起来，并判断是否要换肤
 *         使用该类时要先在业务层自定义一个Application类，然后在其中初始化
 */
public abstract class SkinCompatActivity extends BaseActivity implements LayoutInflaterFactory, ISkinChangeProvider {

    /**
     * 自己的 View 加载类
     */
    private SkinAppCompatViewInflater mAppCompatViewInflater;
    private boolean isInitialized = false;
    private boolean isShouldBeUsed;


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
     * @return 一个由V7包创建的View，如果有皮肤资源则该View同时已经换肤
     */
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {

        // 1、让V7包创建兼容的 View
        View view = createView(parent, name, context, attrs);

        if (view != null) {
            // 2、从拦截到的 View 的属性集合中获取需要换肤的属性集合
            List<SkinAttr> skinAttrs = SkinAttrHelper.getSkinAttrs(context, attrs);

            // 3、生成换肤后的 SkinView
            SkinView skinView = new SkinView(view, skinAttrs);

            // 4、统一交给 SkinManager 管理
            managerSkinView(skinView);

            // 5、判断是否有皮肤，如果有就为这个View换肤
            if (SkinManager.getInstance().isExistSkin()) {
                SkinManager.getInstance().changeOneViewSkin(skinView);
            }

        }
        return view;
    }

    /**
     * 将 SkinView添加到 SkinManager中需要换肤的View集合中，由 SkinManager统一管理
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
     */
    public View createView(View parent, final String name, @NonNull Context context,
                           @NonNull AttributeSet attrs) {

        final boolean isPre21 = Build.VERSION.SDK_INT < 21;
        if (mAppCompatViewInflater == null) {
            mAppCompatViewInflater = new SkinAppCompatViewInflater();
        }


        final boolean inheritContext = isPre21 && shouldInheritContext((ViewGroup) parent);


        if (!isInitialized) {
            isShouldBeUsed = isShouldBeUsed();
            isInitialized = true;
        }

        return mAppCompatViewInflater.createView(parent, name, context, attrs, inheritContext,
                isPre21,
                true,
                isShouldBeUsed);
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

    /**
     * 如果子类Activity有自定义View，则要重写该方法，自己实现自定义View的换肤
     *
     * @param skinResource 皮肤包的资源
     */
    @Override
    public void changeSkin(SkinResource skinResource) {
    }

    @Override
    protected void onDestroy() {
        // 在 activity 被回收的时候释放单例保存的 activity的引用，防止内存泄漏
        SkinManager.getInstance().unregister(this);
        super.onDestroy();
    }


    private boolean isShouldBeUsed() {
        try {
            Class<?> clazz = Class.forName("android.support.v7.widget.VectorEnabledTintResources");
            Method shouldBeUsedMethod = clazz.getDeclaredMethod("shouldBeUsed");
            return (boolean) shouldBeUsedMethod.invoke(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}

