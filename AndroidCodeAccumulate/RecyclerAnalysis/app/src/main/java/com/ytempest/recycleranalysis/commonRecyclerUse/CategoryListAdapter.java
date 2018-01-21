package com.ytempest.recycleranalysis.commonRecyclerUse;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonRecyclerAdapter;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonViewHolder;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.MultiTypeSupport;

import java.util.List;


/**
 * @author ytempest
 *         Description：
 */
public class CategoryListAdapter extends CommonRecyclerAdapter<ChannelData> {


    public CategoryListAdapter(Context context, List<ChannelData> dataList, int layoutId) {
        super(context, dataList, layoutId);
    }

    public CategoryListAdapter(Context context, List<ChannelData> dataList, MultiTypeSupport<ChannelData> multiTypeSupport) {
        super(context, dataList, multiTypeSupport);
    }

    @Override
    protected void bindViewData(final CommonViewHolder holder, ChannelData item) {

        switch (holder.getItemViewType()) {
            case R.layout.channel_list_item_left :
            case R.layout.channel_list_item_right:
                // 绑定数据
                String str = item.getSubscribeCount() + " 订阅 | " +
                        "总帖数 <font color='#FF678D'>" + item.getTotalUpdates() + "</font>";
                holder.setImageByUrl(R.id.iv_channel_icon, new GlideImageLoader("http://s0.pstatp.com/site/image/joke_zone/commenticon_discover@2x.png"))
                        .setText(R.id.tv_channel_text, item.getIntro() + "")
                        .setText(R.id.tv_channel_topic, item.getName() + "")
                        .setText(R.id.tv_channel_update_info, Html.fromHtml(str));

                // 是否是最新
                int visibility = item.isIs_recommend() ? View.VISIBLE : View.GONE;
                holder.setViewVisibility(R.id.iv_recommend_label, visibility);

                // 设置点击事件
                initClickListener(holder);
                break;

            case R.layout.channel_list_item_test:
                holder.setText(R.id.tv_test_item_text, "测试成功")
                        .setImageByUrl(R.id.iv_test_item_image, new GlideImageLoader("http://s0.pstatp.com/site/image/joke_zone/commenticon_discover@2x.png"));

                holder.addViewClickListener(R.id.iv_test_item_image, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "你点击了测试图标", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.addItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "测试成功", Toast.LENGTH_SHORT).show();
                    }
                });
            default:
                break;

        }

    }

    private void initClickListener(final CommonViewHolder holder) {
        holder.addViewClickListener(R.id.iv_channel_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.itemView.getContext(), "你点击了图标", Toast.LENGTH_SHORT).show();
            }
        });

        holder.addViewClickListener(R.id.bt_channel_subscribe, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.itemView.getContext(), "你点击了订阅", Toast.LENGTH_SHORT).show();
            }
        });

        holder.addItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.itemView.getContext(), "你点击了条目", Toast.LENGTH_SHORT).show();
            }
        });

        holder.addItemLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(holder.itemView.getContext(), "你长按条目", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

}
