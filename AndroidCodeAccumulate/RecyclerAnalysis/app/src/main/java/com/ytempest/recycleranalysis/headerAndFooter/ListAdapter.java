package com.ytempest.recycleranalysis.headerAndFooter;

import android.content.Context;
import android.text.Html;
import android.view.View;

import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.commonRecyclerUse.GlideImageLoader;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonRecyclerAdapter;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonViewHolder;
import com.ytempest.recycleranalysis.headerAndFooter.data.ChannelListResult;

import java.util.List;

/**
 * Created by Darren on 2016/12/28.
 * Email: 240336124@qq.com
 * Description: 利用万能通用的Adapter改造后的列表
 */
public class ListAdapter extends CommonRecyclerAdapter<ChannelListResult.DataBean.CategoriesBean.CategoryListBean> {

    public ListAdapter(Context context, List<ChannelListResult.DataBean.
            CategoriesBean.CategoryListBean> datas) {
        super(context, datas, R.layout.channel_list_item_right);
    }


    @Override
    protected void bindViewData(CommonViewHolder holder, ChannelListResult.DataBean.CategoriesBean.CategoryListBean item) {
        // 显示数据
        String str = item.getSubscribe_count() + " 订阅 | " +
                "总帖数 <font color='#FF678D'>" + item.getTotal_updates() + "</font>";
        holder.setText(R.id.tv_channel_text, item.getName())
                .setText(R.id.tv_channel_topic, item.getIntro())
                .setText(R.id.tv_channel_update_info, Html.fromHtml(str));

        // 是否是最新
        if (item.isIs_recommend()) {
            holder.setViewVisibility(R.id.iv_recommend_label, View.VISIBLE);
        } else {
            holder.setViewVisibility(R.id.iv_recommend_label, View.GONE);
        }
        // 加载图片
        holder.setImageByUrl(R.id.iv_channel_icon, new GlideImageLoader(item.getIcon_url()));
    }
}
