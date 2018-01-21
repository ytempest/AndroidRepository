package com.ytempest.framelibrary.skin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.ytempest.framelibrary.skin.attr.SkinView;
import com.ytempest.framelibrary.skin.callback.ISkinChangeListener;
import com.ytempest.framelibrary.skin.config.SkinConfig;
import com.ytempest.framelibrary.skin.config.SkinPreUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ytempest
 *         Description: 皮肤管理类
 */
public class SkinManager {

    private static SkinManager mInstance;
    private Context mContext;
    private SkinResource mSkinResource;

    /**
     * 记录每一个 Activity 下需要换肤的View
     */
    private Map<ISkinChangeListener, List<SkinView>> mSkinViews = new HashMap<>();

    private SkinManager() {
    }

    public static SkinManager getInstance() {
        if (mInstance == null) {
            synchronized (SkinManager.class) {
                if (mInstance == null) {
                    mInstance = new SkinManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();

        // 每一个次打开应用都会到这里，防止皮肤被恶意删除，做一些措施
        // 措施一：如果不存在该皮肤资源文件，则删除皮肤路径
        String curSkinPath = getSkinPath();
        if (!existSkinFile(curSkinPath)) {
            clearSkinInfo();
            return;
        }

        // 措施二：如果获取不了包名也删除皮肤
        String packageName = getPackageName(curSkinPath);
        if (TextUtils.isEmpty(packageName)) {
            clearSkinInfo();
            return;
        }

        // 措施三：最好校验签名，增量更新的时候补上

        // 做一些初始化的操作
        initSkinResource(curSkinPath);
    }

    /**
     * 恢复默认皮肤
     *
     * @return
     */
    public int restoreDefault() {
        // 1. 判断有没有皮肤，如果没有皮肤则返回
        String curSkinPath = getSkinPath();
        if (TextUtils.isEmpty(curSkinPath)) {
            return SkinConfig.SKIN_LOADED;
        }

        // 2. 获取当前运行 apk 的包路径
        String defaultSkinPath = mContext.getPackageResourcePath();

        // 3. 初始化资源管理
        initSkinResource(defaultSkinPath);

        // 4. 改变皮肤
        changeSkin();

        // 5. 删除保存的皮肤状态
        clearSkinInfo();

        return SkinConfig.SKIN_LOADED_SUCCESS;
    }


    /**
     * 加载并更换皮肤
     *
     * @param skinPath 皮肤的路径
     * @return
     */
    public int loadSkin(String skinPath) {

        // 1. 判断皮肤文件是否存在
        if (!existSkinFile(skinPath)) {
            return SkinConfig.SKIN_FILE_NOEXIST;
        }

        // 2. 判断是否能获取皮肤文件的包名
        String packageName = getPackageName(skinPath);
        if (TextUtils.isEmpty(packageName)) {
            return SkinConfig.SKIN_FILE_ERROR;
        }

        // 3. 判断当前皮肤已经是需要换肤的皮肤
        String curSkinPath = getSkinPath();
        if (curSkinPath.equals(skinPath)) {
            return SkinConfig.SKIN_LOADED_SUCCESS;
        }


        // 4. 检验签名，在增量更新再讲

        // 最好把皮肤包复制走，用户不能轻易删除的地方， cache目录下面

        // 初始化资源管理
        initSkinResource(skinPath);

        // 改变皮肤
        changeSkin();

        // 保存皮肤的状态
        saveSkinStatus(skinPath);

        return SkinConfig.SKIN_LOADED_SUCCESS;
    }

    private boolean existSkinFile(String skinPath) {
        File skinFile = new File(skinPath);
        if (!skinFile.exists()) {
            return false;
        }
        return true;
    }

    /**
     * 初始化指定包名的皮肤资源管理类
     *
     * @param curSkinPath
     */
    private void initSkinResource(String curSkinPath) {
        mSkinResource = new SkinResource(mContext, curSkinPath);
    }

    /**
     * 换肤
     */
    private void changeSkin() {
        for (Map.Entry<ISkinChangeListener, List<SkinView>> entry : mSkinViews.entrySet()) {
            List<SkinView> skinViews = entry.getValue();

            for (SkinView skinView : skinViews) {
                skinView.changeSkin();
            }

            ISkinChangeListener skinChangeListener = entry.getKey();
            skinChangeListener.changeSkin(mSkinResource);

        }
    }


    /**
     * 通过 activity 获取 skinView
     *
     * @param skinChangeListener
     * @return
     */
    public List<SkinView> getSkinViews(ISkinChangeListener skinChangeListener) {
        return mSkinViews.get(skinChangeListener);
    }

    /**
     * 如果该 Activity还没有初始化，那就先注册
     *
     * @param skinChangeListener
     * @param skinViews
     */
    public void register(ISkinChangeListener skinChangeListener, List<SkinView> skinViews) {
        mSkinViews.put(skinChangeListener, skinViews);
    }

    /**
     * 获取当前皮肤资源类
     *
     * @return
     */
    public SkinResource getSkinResource() {
        return mSkinResource;
    }

    /**
     * 根据 apk 的路径获取该 apk的包名
     *
     * @param curSkinPath apk 的路径
     * @return apk的包名
     */
    private String getPackageName(String curSkinPath) {
        return mContext.getPackageManager()
                .getPackageArchiveInfo(curSkinPath, PackageManager.GET_ACTIVITIES).packageName;
    }

    public void checkSkinStatus(SkinView skinView) {
        String skinPath = getSkinPath();
        if (!TextUtils.isEmpty(skinPath)) {
            // 如果有皮肤就换肤
            skinView.changeSkin();
        }
    }

    private void saveSkinStatus(String skinPath) {
        // 不使用之前的数据库是不想让一个框架嵌套另外一个框架
        SkinPreUtils.getInstance(mContext).saveSkinPath(skinPath);
    }

    private String getSkinPath() {
        return SkinPreUtils.getInstance(mContext).getSkinPath();
    }

    private void clearSkinInfo() {
        SkinPreUtils.getInstance(mContext).clearSkinInfo();
    }

    /**
     * 防止内存泄漏，移除 activity的引用
     *
     * @param listener 实现了 ISkinChangeListener接口的 Activity
     */
    public void unregister(ISkinChangeListener listener) {
        mSkinViews.remove(listener);
    }
}
