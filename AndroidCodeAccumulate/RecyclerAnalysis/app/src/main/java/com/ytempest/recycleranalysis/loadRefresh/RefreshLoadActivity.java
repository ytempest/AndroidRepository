package com.ytempest.recycleranalysis.loadRefresh;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;


import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonRecyclerAdapter;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonViewHolder;
import com.ytempest.recycleranalysis.divisionUse.GridItemDecoration;
import com.ytempest.recycleranalysis.loadRefresh.widget.LoadRecyclerView;
import com.ytempest.recycleranalysis.loadRefresh.widget.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description:  RecyclerView下拉刷新上拉加载
 */
public class RefreshLoadActivity extends AppCompatActivity implements RefreshRecyclerView.OnRefreshMoreListener, LoadRecyclerView.OnLoadMoreListener {
    private LoadRecyclerView mRecyclerView;
    private List<String> mDataList;
    private HomeAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_load);
        mDataList = new ArrayList<>();
        initData();

        initView();

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        mRecyclerView.addItemDecoration(new GridItemDecoration(this));

        // 获取数据，然后将数据设置到Adapter中
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter = new HomeAdapter(RefreshLoadActivity.this, mDataList);
                mRecyclerView.setAdapter(mAdapter);
            }
        }, 2000);



        /*mAdapter = new HomeAdapter(RefreshLoadActivity.this, mDataList);
        mRecyclerView.setAdapter(mAdapter);*/

        /*mRecyclerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mDataList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });*/
        // mRecyclerView.addItemDecoration(new DividerGridItemDecoration(this));
    }

    private void initView() {
        mRecyclerView = (LoadRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addRefreshViewCreator(new DefaultRefreshCreator());
        mRecyclerView.addLoadViewCreator(new DefaultLoadCreator());
        mRecyclerView.setOnRefreshMoreListener(this);
        mRecyclerView.setOnLoadMoreListener(this);

        // 设置正在获取数据页面和无数据页面
        mRecyclerView.addLoadingView(findViewById(R.id.tv_load_view));
        mRecyclerView.addEmptyView(findViewById(R.id.tv_empty_view));
    }

    protected void initData() {
        for (int i = 'A'; i < 'O'; i++) {
            mDataList.add("" + (char) i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_action_gridview:
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
                break;
            case R.id.id_action_listview:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.stopRefresh();
            }
        }, 2000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
                mRecyclerView.stopLoad();
                mAdapter.notifyDataSetChanged();
            }
        }, 2000);
    }

    class HomeAdapter extends CommonRecyclerAdapter<String> {

        public HomeAdapter(Context context, List<String> data) {
            super(context, data, R.layout.item_home);
        }


        @Override
        protected void bindViewData(CommonViewHolder holder, String item) {
            holder.setText(R.id.tv_num, item);
        }
    }
}
