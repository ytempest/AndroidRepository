package com.ytempest.framelibrary.skin.callback;

import com.ytempest.framelibrary.skin.SkinResource;

/**
 * @author ytempest
 *         Description: 让第三方的自定义View 回调该方法实现自定义View的换肤
 */
public interface ISkinChangeListener {
    /**
     * 从皮肤包中获取相应的皮肤属性，注入到自定义 View 中
     *
     * @param skinResource 皮肤包的资源
     */
    void changeSkin(SkinResource skinResource);

}
