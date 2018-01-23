package com.ytempest.bannerviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ytempest.widget.binnerview.BannerAdapter;
import com.ytempest.widget.binnerview.BannerView;
import com.ytempest.widget.binnerview.BannerViewPager;


public class MainActivity extends Activity {

    private String[] mBannerPaths = {"http://p9.pstatp.com/origin/e59001214a23d34b940",
            "http://p9.pstatp.com/origin/ef400087b7d7fbdec85"};
    private String[] mBannerText = {"挑战花式讲段子1", "挑战花式讲段子5", "天蝎宝宝嗨起来~"};
    private int[] mBannerImages = {R.drawable.one,R.drawable.one, R.drawable.two};

    private BannerView mBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBannerView = (BannerView) findViewById(R.id.banner_view);

        mBannerView.setAdapter(new BannerAdapter() {
            @Override
            public View getView(int position, View convertView) {
                if(convertView == null){
                    convertView = new ImageView(MainActivity.this);
                }
                ImageView imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setBackgroundResource(mBannerImages[position]);
                return convertView;
            }

            @Override
            public int getCount() {
                return mBannerImages.length;
            }

            @Override
            public String getBannerText(int position) {
                return mBannerText[position];
            }
        });

        mBannerView.setOnBannerItemClickListener(new BannerViewPager.BannerItemClickListener() {
            @Override
            public void onPageClick(int position) {
                Toast.makeText(MainActivity.this, "you click " + mBannerText[position], Toast.LENGTH_SHORT).show();
            }
        });




    }
}
