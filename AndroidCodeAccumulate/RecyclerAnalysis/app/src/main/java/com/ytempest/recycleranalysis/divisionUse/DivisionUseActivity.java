package com.ytempest.recycleranalysis.divisionUse;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ytempest.recycleranalysis.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class DivisionUseActivity extends AppCompatActivity {

    private static final String TAG = "DivisionUseActivity";
    private RecyclerView mRecyclerView;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_use);

        initData();

        initView();

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
                mRecyclerView.setLayoutManager(new GridLayoutManager(DivisionUseActivity.this, 3, RecyclerView.VERTICAL, false));
//                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                mRecyclerView.removeItemDecorationAt(0);
//                mRecyclerView.addItemDecoration(new GridItemDecoration(DivisionUseActivity.this));
                mRecyclerView.addItemDecoration(new GridItemDecoration(DivisionUseActivity.this));

                break;
            case R.id.id_action_listview:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.removeItemDecorationAt(0);
                DividerItemDecoration decoration = new DividerItemDecoration(DivisionUseActivity.this);
                decoration.setDrawable(getResources().getDrawable(R.drawable.rv_division_image));
                mRecyclerView.addItemDecoration(decoration);
                break;
            default:
                break;

        }
        return true;
    }

    private void initData() {
        mDatas = new ArrayList<>();
        for (int i = 1; i <= 26; i++) {
            mDatas.add("" + i);
        }
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(DivisionUseActivity.this, LinearLayoutManager.VERTICAL, false));

        mRecyclerView.setAdapter(new RecyclerAdapter(this, mDatas));

        DividerItemDecoration decoration = new DividerItemDecoration(DivisionUseActivity.this);
        decoration.setDrawable(getResources().getDrawable(R.drawable.rv_division_image));
        mRecyclerView.addItemDecoration(decoration);

    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

        private List<String> mDataList;
        private LayoutInflater mInflater;

        public RecyclerAdapter(Context context, List<String> datas) {
            mInflater = LayoutInflater.from(context);
            mDataList = datas;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerViewHolder viewHolder = new RecyclerViewHolder(mInflater.inflate(R.layout.item_recycler_view, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            holder.textView.setText(mDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        public class RecyclerViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public RecyclerViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.tv_recycler_view);
            }
        }
    }

}
