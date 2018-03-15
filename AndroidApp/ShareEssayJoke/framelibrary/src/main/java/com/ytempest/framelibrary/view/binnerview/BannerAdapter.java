package com.ytempest.framelibrary.view.binnerview;


import android.view.View;

/**
 * @author ytempest
 *         Description：BannerViewPager的适配器，用于兼容扩展的功能
 */
public abstract class BannerAdapter {
    /**
     * 获取根据位置获取ViewPager里面的子View
     */
    public abstract View getView(int position, View convertView);

    /**
     * 5.获取轮播的数量
     */
    public abstract int getCount();

    /**
     * 6.根据位置获取广告位描述
     */
    public String getBannerText(int position){
        return "";
    }
}
