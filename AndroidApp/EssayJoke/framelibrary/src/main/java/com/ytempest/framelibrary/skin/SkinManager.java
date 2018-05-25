package com.ytempest.framelibrary.skin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.ytempest.framelibrary.skin.attr.SkinView;
import com.ytempest.framelibrary.skin.callback.ISkinChangeProvider;
import com.ytempest.framelibrary.skin.callback.OnSkinChangeListener;
import com.ytempest.framelibrary.skin.config.SkinConfig;
import com.ytempest.framelibrary.skin.config.SkinUtils;
import com.ytempest.framelibrary.skin.scheduler.Schedulers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ytempest
 *         Description: 皮肤管理类；负责管理每一个Activity下所有需要换肤的View，提供加载新皮肤以及恢复默认皮肤的功能
 */
public class SkinManager {

    private static final String TAG = "SkinManager";

    private volatile static SkinManager sSkinManager;
    private Context mContext;
    private SkinResource mSkinResource;
    private OnSkinChangeListener mOnSkinChangeListener;


    /**
     * 记录每一个 Activity 下需要换肤的View
     */
    private Map<ISkinChangeProvider, List<SkinView>> mSkinViews = new HashMap<>();

    private SkinManager() {
    }

    public static SkinManager getInstance() {
        if (sSkinManager == null) {
            synchronized (SkinManager.class) {
                if (sSkinManager == null) {
                    sSkinManager = new SkinManager();
                }
            }
        }
        return sSkinManager;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();

        // 每一个次打开应用都会到这里，防止皮肤被恶意删除，做一些措施
        // 措施一：如果不存在该皮肤资源文件，则删除皮肤路径
        String curSkinPath = getSkinPath();
        if (!isExistSkin()) {
            clearSkinPath();
            return;
        }

        // 措施二：如果获取不了包名也删除皮肤
        String packageName = getPackageName(curSkinPath);
        if (TextUtils.isEmpty(packageName)) {
            clearSkinPath();
            return;
        }

        // 措施三：最好校验签名，增量更新的时候补上

        // 做一些初始化的操作
        initSkinResource(curSkinPath);
    }

    /**
     * 加载并更换皮肤
     *
     * @param skinPath 皮肤的路径
     */
    public void loadSkin(final String skinPath) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 1、检查皮肤文件是否有问题
                if (!checkSkinFile(skinPath)) {
                    return;
                }

                // 2、回调开始换肤的方法
                callSkinChangeStart();

                // 4、检验签名，在增量更新再讲

                // 5、最好把皮肤包复制走，用户不能轻易删除的地方， cache目录下面

                // 6、初始化资源管理
                initSkinResource(skinPath);

                // 7、改变皮肤
                startChangeSkin();

                // 8、保存皮肤的状态
                saveSkinPath(skinPath);

                // 9、回调换肤成功后的方法
                callSkinChangeSucceed();
            }
        };

        Schedulers.asyncThread().schedule(task);
    }


    /**
     * 恢复默认皮肤
     */
    public void resetDefaultSkin() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 1、判断有没有皮肤，如果没有皮肤则表示已经是默认皮肤了
                if (!isExistSkin()) {
                    callSkinError(SkinConfig.SKIN_HAD_LOADED);
                    return;
                }

                // 2、回调开始换肤的方法
                callSkinChangeStart();

                // 2、获取当前运行 apk 的资源包路径
                String defaultSkinPath = mContext.getPackageResourcePath();

                // 3、初始化资源管理
                initSkinResource(defaultSkinPath);

                // 4、改变皮肤
                startChangeSkin();

                // 5、删除保存的皮肤状态
                clearSkinPath();

                // 6、回调换肤成功后的方法
                callSkinChangeSucceed();
            }
        };

        Schedulers.asyncThread().schedule(task);
    }


    /**
     * 检测皮肤文件是否存在问题，如果没有问题就返回true
     *
     * @return 返回true代表皮肤文件没问题
     */
    private boolean checkSkinFile(String skinPath) {

        if (TextUtils.isEmpty(skinPath)) {
            throw new IllegalStateException("Path of skin have problem, please check it ");
        }

        // 1、判断皮肤文件是否存在
        if (!isExistSkin(skinPath)) {
            // 如果不存在
            callSkinError(SkinConfig.SKIN_NOT_EXIST);
            return false;
        }

        // 2、判断是否能获取皮肤文件的包名
        String packageName = getPackageName(skinPath);
        if (TextUtils.isEmpty(packageName)) {
            callSkinError(SkinConfig.SKIN_FILE_ERROR);
            return false;
        }

        // 3、判断当前皮肤已经是需要换肤的皮肤
        String curSkinPath = getSkinPath();
        if (curSkinPath.equals(skinPath)) {
            callSkinError(SkinConfig.SKIN_HAD_LOADED);
            return false;
        }

        return true;
    }

    /**
     * 回到监听器中的 onError()方法，并将该方法切换到主线程中执行
     */
    private void callSkinError(final int errorCode) {
        if (mOnSkinChangeListener != null) {
            Schedulers.mainThread().schedule(new Runnable() {
                @Override
                public void run() {
                    mOnSkinChangeListener.onError(errorCode);
                }
            });
        }
    }

    /**
     * 回到监听器中的 onStart()方法，并将该方法切换到主线程中执行
     */
    private void callSkinChangeStart() {
        if (mOnSkinChangeListener != null) {
            Schedulers.mainThread().schedule(new Runnable() {
                @Override
                public void run() {
                    mOnSkinChangeListener.onStart();
                }
            });
        }
    }

    /**
     * 回到监听器中的 onSucceed()方法，并将该方法切换到主线程中执行
     */
    private void callSkinChangeSucceed() {
        if (mOnSkinChangeListener != null) {
            Schedulers.mainThread().schedule(new Runnable() {
                @Override
                public void run() {
                    mOnSkinChangeListener.onSucceed();
                }
            });
        }
    }

    /**
     * 初始化指定包名的皮肤资源管理类
     */
    private void initSkinResource(String curSkinPath) {
        mSkinResource = new SkinResource(mContext, curSkinPath);
    }

    /**
     * 为已经注册的所有Activity下的所有View换肤
     */
    private void startChangeSkin() {
        Schedulers.mainThread().schedule(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<ISkinChangeProvider, List<SkinView>> entry : mSkinViews.entrySet()) {
                    final ISkinChangeProvider iSkinChangeProvider = entry.getKey();
                    final List<SkinView> skinViews = entry.getValue();

                    for (SkinView skinView : skinViews) {
                        skinView.changeSkin();
                    }

                    // 回调方法，让调用者自己处理View的自定义属性
                    iSkinChangeProvider.changeSkin(mSkinResource);
                }
            }
        });
    }


    /**
     * 通过 activity 获取 skinView
     *
     * @return 当前Activity下所有需要换肤的View
     */
    public List<SkinView> getSkinViews(ISkinChangeProvider skinChangeListener) {
        return mSkinViews.get(skinChangeListener);
    }


    /**
     * 获取当前皮肤资源类
     */

    public SkinResource getSkinResource() {
        return mSkinResource;
    }

    /**
     * 根据 apk 的路径获取该 apk的包名
     *
     * @param curSkinPath apk 的路径
     * @return 指定apk路径的apk的包名
     */
    private String getPackageName(String curSkinPath) {
        return mContext.getPackageManager()
                .getPackageArchiveInfo(curSkinPath, PackageManager.GET_ACTIVITIES).packageName;
    }

    public void changeOneViewSkin(SkinView skinView) {
        skinView.changeSkin();
    }


    public boolean isExistSkin() {
        String skinPath = getSkinPath();
        // 如果有该皮肤文件
        return isExistSkin(skinPath);
    }

    private boolean isExistSkin(String skinPath) {
        // 如果有该皮肤文件
        return !TextUtils.isEmpty(skinPath) && new File(skinPath).exists();
    }


    private String getSkinPath() {
        return SkinUtils.getInstance(mContext).getSkinPath();
    }

    private void saveSkinPath(String skinPath) {
        // 不使用之前的数据库是不想让一个框架嵌套另外一个框架
        SkinUtils.getInstance(mContext).saveSkinPath(skinPath);
    }

    private void clearSkinPath() {
        SkinUtils.getInstance(mContext).clearSkinPath();
    }


    /**
     * 如果该 Activity还没有初始化，那就先注册
     */
    public void register(ISkinChangeProvider skinChangeListener, List<SkinView> skinViews) {
        if (mSkinViews.containsKey(skinChangeListener)) {
            throw new IllegalArgumentException("This ISkinChangeProvider had registered!");
        }
        mSkinViews.put(skinChangeListener, skinViews);
    }

    /**
     * 防止内存泄漏，移除 activity的引用
     *
     * @param listener 实现了 ISkinChangeListener接口的 Activity
     */
    public void unregister(ISkinChangeProvider listener) {
        if (!mSkinViews.containsKey(listener)) {
            return;
        }
        mSkinViews.remove(listener);
    }


    public void addOnSkinChangeListener(OnSkinChangeListener listener) {
        mOnSkinChangeListener = listener;
    }

}
