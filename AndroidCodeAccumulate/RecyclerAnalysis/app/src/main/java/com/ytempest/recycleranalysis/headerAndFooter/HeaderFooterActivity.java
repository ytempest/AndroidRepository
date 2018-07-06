package com.ytempest.recycleranalysis.headerAndFooter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.divisionUse.DividerItemDecoration;
import com.ytempest.widget.binnerview.BannerAdapter;
import com.ytempest.widget.binnerview.BannerView;

import java.util.ArrayList;
import java.util.List;

public class HeaderFooterActivity extends AppCompatActivity {
    private WrapRecyclerView mRecyclerView;

    private String[] mBannerText = {"挑战花式讲段子", "天蝎宝宝嗨起来~"};
    private String[] mBannerPaths = {"http://p9.pstatp.com/origin/e59001214a23d34b940",
            "http://p9.pstatp.com/origin/ef400087b7d7fbdec85"};

    private List<String> mDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_footer);
        mRecyclerView = (WrapRecyclerView) findViewById(R.id.rv_header_footer);
/*        // 设置显示分割 ListView样式
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false));
        // 添加分割线
        GridItemDecoration decor = new GridItemDecoration(this);
        decor.setDrawable(getResources().getDrawable(R.drawable.rv_division_image));
        mRecyclerView.addItemDecoration(decor);*/

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration = new DividerItemDecoration(this);
        decoration.setDrawable(getResources().getDrawable(R.drawable.rv_division_image));
        mRecyclerView.addItemDecoration(decoration);

        initData();

        ListAdapter listAdapter = new ListAdapter(this, mDataList);
        mRecyclerView.setAdapter(listAdapter);

        mRecyclerView.addHeaderView(getBannerView());
        addFooterView();
    }

    private void initData() {
        mDataList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            mDataList.add("" + i);
        }
    }

    private void addFooterView() {
        TextView tv = new TextView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setBackgroundColor(Color.GREEN);
        tv.setTextSize(20);
        tv.setText("AAAAAAAAA");
        tv.setLayoutParams(params);
        mRecyclerView.addFooterView(tv);
    }


    /**
     * 添加Banner轮播图
     */
    private View getBannerView() {

        BannerView bannerView = (BannerView) (LayoutInflater.from(this)
                .inflate(R.layout.layout_banner_view, mRecyclerView, false));

        bannerView.setAdapter(new BannerAdapter() {
            @Override
            public View getView(int position, View convertView) {
                if (convertView == null) {
                    convertView = new ImageView(HeaderFooterActivity.this);
                }
                ImageView imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(HeaderFooterActivity.this).load(mBannerPaths[position]).into(imageView);
                return imageView;
            }

            @Override
            public int getCount() {
                return mBannerPaths.length;
            }

            @Override
            public String getBannerText(int position) {
                return mBannerText[position];
            }
        });

        return bannerView;
    }

}

