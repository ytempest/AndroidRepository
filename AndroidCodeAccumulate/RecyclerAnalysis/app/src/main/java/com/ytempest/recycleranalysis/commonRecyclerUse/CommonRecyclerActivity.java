package com.ytempest.recycleranalysis.commonRecyclerUse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.MultiTypeSupport;
import com.ytempest.recycleranalysis.division.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class CommonRecyclerActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    List<ChannelData> mDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_recycler);

        initData();

        initView();
    }

    private void initData() {
        mDataList = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            mDataList.add(new ChannelData(i, i, i, i % 3 == 0, i));
        }
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(CommonRecyclerActivity.this));
        // 设置分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(CommonRecyclerActivity.this));
        // 实例化多布局支持接口
        MultiTypeSupport<ChannelData> multiTypeSupport = new MultiTypeSupport<ChannelData>() {
            @Override
            public int getLayoutId(ChannelData item, int position) {
                if (position % 4 == 0) {
                    return R.layout.channel_list_item_right;
                } else if (position % 7 == 0 && position % 4 != 0) {
                    return R.layout.channel_list_item_test;
                }
                return R.layout.channel_list_item_left;
            }
        };
        mRecyclerView.setAdapter(new CategoryListAdapter(CommonRecyclerActivity.this, mDataList, multiTypeSupport));
    }
}
