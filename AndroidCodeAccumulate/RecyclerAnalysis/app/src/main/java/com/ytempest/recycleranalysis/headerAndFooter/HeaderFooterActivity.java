package com.ytempest.recycleranalysis.headerAndFooter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.ytempest.recycleranalysis.R;
import com.ytempest.recycleranalysis.divisionUse.GridItemDecoration;
import com.ytempest.recycleranalysis.headerAndFooter.data.ChannelListResult;
import com.ytempest.widget.binnerview.BannerAdapter;
import com.ytempest.widget.binnerview.BannerView;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderFooterActivity extends AppCompatActivity {
    private WrapRecyclerView mRecyclerView;
    private OkHttpClient mOkHttpClient;
    private static Handler mHandler = new Handler();

    private String[] mBannerText = {"挑战花式讲段子", "天蝎宝宝嗨起来~"};
    private String[] mBannerPaths = {"http://p9.pstatp.com/origin/e59001214a23d34b940",
            "http://p9.pstatp.com/origin/ef400087b7d7fbdec85"};

    List<ChannelListResult.DataBean.CategoriesBean.CategoryListBean> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_footer);
        mRecyclerView = (WrapRecyclerView) findViewById(R.id.rv_header_footer);
        // 设置显示分割 ListView样式
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // 添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration(this));
        mOkHttpClient = new OkHttpClient();

        requestListData();
    }


    /**
     * 请求列表数据
     */
    private void requestListData() {

        // 利用Okhttp去获取网络数据
        Request.Builder builder = new Request.Builder();
        builder.url("http://is.snssdk.com/2/essay/discovery/v3/?iid=6152551759&channel=360&aid=7" +
                "&app_name=joke_essay&version_name=5.7.0&ac=wifi&device_id=30036118478&device_brand=Xiaomi&update_version_code=5701&" +
                "manifest_version_code=570&longitude=113.000366&latitude=28.171377&device_platform=android");

        mOkHttpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                // Gson 解析成对象
                final ChannelListResult channelList = new Gson().fromJson(result, ChannelListResult.class);
                // 获取列表数据
                final List<ChannelListResult.DataBean.CategoriesBean.CategoryListBean> categoryList =
                        channelList.getData().getCategories().getCategory_list();
                // 该方法不是在主线程中
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showListData(categoryList);
                        addBannerView();
                        addFooterView();
                    }
                });
            }
        });
    }

    private void addFooterView() {
        TextView tv = new TextView(this);
        tv.setTextSize(20);
        tv.setText("AAAAAAAAA");
        mRecyclerView.addFooterView(tv);
    }


    /**
     * 添加Banner轮播图
     */
    private void addBannerView() {

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

        mRecyclerView.addHeaderView(bannerView);
    }

    /**
     * 显示列表数据
     */
    private void showListData(List<ChannelListResult.DataBean.CategoriesBean.CategoryListBean> categoryList) {
        mData = categoryList;

        final ListAdapter listAdapter = new ListAdapter(this, categoryList);

        // 添加头部和底部 需要 包裹Adapter，才能添加头部和底部
        /*WrapRecyclerAdapter wrapRecyclerAdapter = new WrapRecyclerAdapter(listAdapter);
        mRecyclerView.setAdapter(wrapRecyclerAdapter);

        // 添加头部和底部
        wrapRecyclerAdapter.addHeaderView(LayoutInflater.from(this).inflate(R.layout.layout_header_footer,mRecyclerView,false));
        wrapRecyclerAdapter.addHeaderView(LayoutInflater.from(this).inflate(R.layout.layout_header_footer,mRecyclerView,false));
        wrapRecyclerAdapter.addFooterView(LayoutInflater.from(this).inflate(R.layout.layout_header_footer,mRecyclerView,false));
        */

        mRecyclerView.setAdapter(listAdapter);
        // 只能写在setAdapter之后，否则没效果，也可以选择抛异常
        /*mRecyclerView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.layout_header_footer,mRecyclerView,false));
        mRecyclerView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.layout_header_footer,mRecyclerView,false));
        mRecyclerView.addFooterView(LayoutInflater.from(this).inflate(R.layout.layout_header_footer,mRecyclerView,false));*/
    }
}

