package com.ytempest.andfixdemo.andfix;

import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * @author ytempest
 *         Description：用于加载差分包，调用修复方法的管理类
 */
public class PatchManager {
    private final Context mContext;
    private final AndFixManager mFixManager;

    public PatchManager(Context context) {
        this.mContext = context;
        mFixManager = new AndFixManager(context);
    }


    /**
     * 加载差分包
     */
    public void loadPatch(String path) {
        File patch = new File(path);
        loadPatch(new Patch(patch));
    }

    /**
     * 加载差分包
     */
    public void loadPatch(Patch patch) {
        // 获取当前上下文的类加载器
        ClassLoader classLoader = mContext.getClassLoader();

        // 遍历所有修复了bug的Class，然后交给AndFixManager去进行修复
        for (List<String> clazzList : patch.getClassMap().values()) {
            mFixManager.fix(patch.getPatchFile(), classLoader, clazzList);
        }
    }

}

