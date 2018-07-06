package com.ytempest.recycleranalysis.headerAndFooter;

import android.content.Context;
import android.text.Html;
import android.view.View;

import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.commonRecyclerUse.GlideImageLoader;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonRecyclerAdapter;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonViewHolder;

import java.util.List;

/**
 * Created by Darren on 2016/12/28.
 * Email: 240336124@qq.com
 * Description: 利用万能通用的Adapter改造后的列表
 */
public class ListAdapter extends CommonRecyclerAdapter<String> {

    public ListAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.channel_list_item_right);
    }


    @Override
    protected void bindViewData(CommonViewHolder holder, String item) {
        // 显示数据
        String str = item + " 订阅 | " +
                "总帖数 <font color='#FF678D'>" + item + "</font>";
        holder.setText(R.id.tv_channel_text, item)
                .setText(R.id.tv_channel_topic, item)
                .setText(R.id.tv_channel_update_info, Html.fromHtml(str));

        // 是否是最新
        if (Integer.parseInt(item) % 4 == 0) {
            holder.setViewVisibility(R.id.iv_recommend_label, View.VISIBLE);
        } else {
            holder.setViewVisibility(R.id.iv_recommend_label, View.GONE);
        }
        // 加载图片
        holder.setImageByUrl(R.id.iv_channel_icon, new GlideImageLoader("http://www.vvfeng.com/data/upload/ueditor/20170421/58f9b7d802992.jpg"));
    }
}
