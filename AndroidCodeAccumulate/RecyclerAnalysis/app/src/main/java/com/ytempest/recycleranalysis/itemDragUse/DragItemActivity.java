package com.ytempest.recycleranalysis.itemDragUse;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonRecyclerAdapter;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonViewHolder;
import com.ytempest.recycleranalysis.divisionUse.GridItemDecoration;
import com.ytempest.recycleranalysis.headerAndFooter.WrapRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ytempest
 *         Description:  列表条目拖动排序和删除
 */
public class DragItemActivity extends AppCompatActivity {
    private String TAG = "DragItemActivity";

    private WrapRecyclerView mRecyclerView;
    private HomeAdapter mAdapter;
    private List<ItemBean> mItems = new ArrayList<ItemBean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_item);

        initData();

        mRecyclerView = (WrapRecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new HomeAdapter(this, mItems);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addEmptyView(findViewById(R.id.tv_empty_view));

        GridItemDecoration gridItemDecoration = new GridItemDecoration(this);
        gridItemDecoration.setDrawable(getResources().getDrawable(R.drawable.rv_division_image));
        mRecyclerView.addItemDecoration(gridItemDecoration);

        final DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);

        // 实现左边侧滑删除 和 拖动排序
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

            /**
             * 设置拖动的Flags以及删除的Flags
             */
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                // 代表只能是向左侧滑删除，当前可以是这样ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT
                int swipeFlags = ItemTouchHelper.LEFT;

                // 默认只是上下拖动
                int dragFlags = dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    // 如果是网格布局就再添加左右拖动的Flags
                    dragFlags = dragFlags | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                }

                return makeMovementFlags(dragFlags, swipeFlags);
            }

            /**
             * 拖动的时候不断的回调方法
             */
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // 获取原来的位置
                int fromPosition = viewHolder.getAdapterPosition();
                // 得到目标的位置
                int targetPosition = target.getAdapterPosition();
                if (fromPosition < targetPosition) {
                    for (int i = fromPosition; i < targetPosition; i++) {
                        // 改变实际的数据集
                        Collections.swap(mItems, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > targetPosition; i--) {
                        // 改变实际的数据集
                        Collections.swap(mItems, i, i - 1);
                    }
                }
                mAdapter.notifyItemMoved(fromPosition, targetPosition);
                return true;
            }


            /**
             * 当条目侧滑删除后会回调的方法
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // 获取当前删除的位置
                int position = viewHolder.getAdapterPosition();
                // adapter 更新notify当前位置删除
                mItems.remove(position);
                mAdapter.notifyItemRemoved(position);
            }

            /**
             * 如果条目被选择（拖动或滑动删除），就会回调该方法
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    // ItemTouchHelper.ACTION_STATE_IDLE 看看源码解释就能理解了
                    // 侧滑或者拖动的时候背景设置为灰色

                }
            }

            /**
             * 当条目被完全移除以及移除动画完全执行完就会调用该方法
             */
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 设置位移到0，解决移除条目后，界面复用后导致复用的条目只显示空白
                viewHolder.itemView.setTranslationX(0);

            }
        });
        // 这个就不多解释了，就这么attach
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    protected void initData() {
        for (int i = 0; i < 1; i++) {
            mItems.add(new ItemBean(i * 8 + 0, "收款", R.drawable.takeout_ic_category_brand));
            mItems.add(new ItemBean(i * 8 + 1, "转账", R.drawable.takeout_ic_category_flower));
            mItems.add(new ItemBean(i * 8 + 2, "余额宝", R.drawable.takeout_ic_category_fruit));
            mItems.add(new ItemBean(i * 8 + 3, "手机充值", R.drawable.takeout_ic_category_medicine));
            mItems.add(new ItemBean(i * 8 + 4, "医疗", R.drawable.takeout_ic_category_motorcycle));
            mItems.add(new ItemBean(i * 8 + 5, "彩票", R.drawable.takeout_ic_category_public));
            mItems.add(new ItemBean(i * 8 + 6, "电影", R.drawable.takeout_ic_category_store));
            mItems.add(new ItemBean(i * 8 + 7, "游戏", R.drawable.takeout_ic_category_sweet));
        }
        mItems.add(new ItemBean(mItems.size(), "更多", R.drawable.takeout_ic_more));
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

    class HomeAdapter extends CommonRecyclerAdapter<ItemBean> {

        public HomeAdapter(Context context, List<ItemBean> data) {
            super(context, data, R.layout.item_drag_sort_delete);
        }

        @Override
        protected void bindViewData(CommonViewHolder holder, ItemBean item) {
            holder.setText(R.id.item_text, item.text);
            holder.setImageResource(R.id.item_img, item.icon);
        }
    }

    public class ItemBean {
        public int id;
        public String text;
        public int icon;

        public ItemBean(int id, String text, int icon) {
            this.id = id;
            this.text = text;
            this.icon = icon;
        }
    }
}
