package com.ytempest.daydayantis.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.view.recyclerview.division.DividerItemDecoration;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.activity.DetailLinkActivity;
import com.ytempest.daydayantis.activity.MainActivity;
import com.ytempest.daydayantis.fragment.adapter.HotInfoAdapter;
import com.ytempest.daydayantis.fragment.mode.HomeDataResult;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;


/**
 * @author ytempest
 *         Description：主页的Fragment
 */
public class HomeFragment extends BaseFragment {


    @ViewById(R.id.ll_home_root)
    private LinearLayout mRootView;

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

    private HomeDataResult mHomeDataResult;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        mRvHotInfo.setLayoutManager(new LinearLayoutManager(mContext));
        mRvHotInfo.addItemDecoration(new DividerItemDecoration(mContext));

        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(mContext, mRootView)
                        .hideLeftIcon()
                        .setTitle(getResources().getString(R.string.app_name))
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .build();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 向服务器请求主页的数据
        requestHomeData();
    }

    private void requestHomeData() {
        HttpUtils.with(mContext)
                .addParam("appid", "1")
                .url("http://v2.ffu365.com/index.php?m=Api&c=Index&a=home")
                .post()
                .execute(new HttpCallBack<HomeDataResult>() {
                    @Override
                    public void onPreExecute() {

                    }

                    /**
                     * 这几个回调方法一般不是运行在主线程中的，但是OnHttpEngine做了处理，
                     * 把方法放到了主线程中
                     */
                    @Override
                    public void onSuccess(HomeDataResult result) {
                        mHomeDataResult = result;
                        showHotInfo(result.getData());
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    private void showHotInfo(final HomeDataResult.DataBean result) {
                        mRvHotInfo.setAdapter(new HotInfoAdapter(mContext, result.getNews_list(), R.layout.item_rv_hot_info));

                        LoaderOptions loaderOptions =
                                new LoaderOptions.Builder()
                                        .errorDrawableId(R.drawable.image_loading_error)
                                        .placeHolder(R.drawable.image_loading_error)
                                        .build();

                        ImageLoaderManager.getInstance().showImage(mIvAdvertise,
                                result.getAd_list().get(0).getImage(), loaderOptions);

                        ImageLoaderManager.getInstance().showImage(mIvRecommend,
                                result.getCompany_list().get(0).getImage(), loaderOptions);


                    }
                });
    }

    @OnClick(R.id.iv_advertise)
    private void onIvAdvertiseClick(View view) {
        String url = mHomeDataResult.getData().getAd_list().get(0).getLink();
        startActivity(getDetailLinkIntent(url));
    }

    @OnClick(R.id.iv_recommend)
    private void onIvRecommendClick(View view) {
        String url = mHomeDataResult.getData().getCompany_list().get(0).getLink();
        startActivity(getDetailLinkIntent(url));
    }


    public Intent getDetailLinkIntent(String url) {
        Intent intent = new Intent(mContext, DetailLinkActivity.class);
        intent.putExtra(DetailLinkActivity.URL_KEY, url);
        return intent;
    }
}

