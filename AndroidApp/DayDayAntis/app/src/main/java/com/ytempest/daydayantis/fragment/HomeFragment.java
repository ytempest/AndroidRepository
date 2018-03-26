package com.ytempest.daydayantis.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.view.recyclerview.division.DividerItemDecoration;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.fragment.adapter.HotInfoAdapter;
import com.ytempest.daydayantis.fragment.mode.HomeDataResult;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.http.OkHttpEngine;

import java.util.List;


/**
 * @author ytempest
 *         Description：
 */
public class HomeFragment extends BaseFragment {

    private static String TAG = "HomeFragment";

    @ViewById(R.id.iv_advertise)
    private ImageView mIvAdvertise;
    @ViewById(R.id.iv_recommend)
    private ImageView mIvRecommend;

    @ViewById(R.id.ll_labour)
    private View mLlLabour;
    @ViewById(R.id.ll_insurance)
    private View mLlInsurance;
    @ViewById(R.id.ll_technology)
    private View mLlTechnology;
    @ViewById(R.id.ll_equipment)
    private View mLlEquipment;
    @ViewById(R.id.ll_league)
    private View mLlLeague;
    @ViewById(R.id.ll_cost)
    private View mLlCost;
    @ViewById(R.id.rv_hot_info)
    private RecyclerView mRvHotInfo;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        mRvHotInfo.setLayoutManager(new LinearLayoutManager(mContext));
        mRvHotInfo.addItemDecoration(new DividerItemDecoration(mContext));
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        requestHomeData();
    }

    private void requestHomeData() {
        Log.e(TAG, "requestHomeData: ");
        HttpUtils.with(mContext)
                .addParam("appid", "1")
                .url("http://v2.ffu365.com/index.php?m=Api&c=Index&a=home")
                .post()
                .execute(new HttpCallBack<HomeDataResult>() {
                    @Override
                    public void onPreExecute() {

                    }

                    @Override
                    public void onSuccess(HomeDataResult result) {
                        showHotInfo(result.getData());
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    private void showHotInfo(final HomeDataResult.DataBean result) {
                        Log.e(TAG, "showHotInfo: ");
                        mRvHotInfo.setAdapter(new HotInfoAdapter(mContext, result.getNews_list(), R.layout.item_rv_hot_info));

            //        Log.e(TAG, "result.getAd_list().get(0).getImage() --> " + result.getAd_list().get(0).getImage());
            //        Log.e(TAG, "result.getCompany_list().get(0).getImage() -->  " + result.getCompany_list().get(0).getImage());
                    /*Glide.with(HomeFragment.this)
                            .load(result.getAd_list().get(0).getImage());
                    Glide.with(HomeFragment.this)
                            .load(result.getCompany_list().get(0).getImage());*/


                    }
                });

    }



}
