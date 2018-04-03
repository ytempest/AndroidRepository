package com.ytempest.daydayantis.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.activity.DetailLinkActivity;
import com.ytempest.daydayantis.data.HomeDataResult;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class HotInfoAdapter extends CommonRecyclerAdapter<HomeDataResult.DataBean.NewsListBean> {

    private Context mContext;

    public HotInfoAdapter(Context context, List<HomeDataResult.DataBean.NewsListBean> dataList) {
        super(context, dataList, R.layout.item_rv_hot_info);
        mContext = context;
    }

    @Override
    protected void bindViewData(final CommonViewHolder holder, final HomeDataResult.DataBean.NewsListBean item) {
        holder.setText(R.id.tv_hot_info, item.getTitle())
                .setText(R.id.tv_date, item.getCreate_time());

        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = item.getLink();
                Intent intent = new Intent(mContext, DetailLinkActivity.class);
                intent.putExtra(DetailLinkActivity.URL_KEY, url);
                mContext.startActivity(intent);
            }
        });
    }
}
