package com.ytempest.baselibrary.navigation;

/**
 * @author ytempest
 *         Description: 导航条的规范
 */
public interface INavigationBar {

    /**
     * 头部的规范
     *
     * @return 需要绑定的布局id
     */
    public int bindLayoutId();


    /**
     * 绑定navigation的参数
     */
    public void applyView();
}
