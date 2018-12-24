package com.ytempest.applist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ytempest.applist.list.TopicResult;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.baselibrary.view.recyclerview.division.DividerItemDecoration;
import com.ytempest.framelibrary.PicturesLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private List<TopicResult> mTopicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Random random = new Random();
        mRecyclerView = findViewById(R.id.recycler_view);
        mTopicList = new ArrayList<>();

        TopicResult result = new TopicResult();
        result.setPictureList(new ArrayList<String>());
        mTopicList.add(result);
        for (int i = 0; i < 10; i++) {
            TopicResult topicResult = new TopicResult();
            List<String> pictureList = new ArrayList<>();
            int k = random.nextInt(10);
            for (; k > 0; k--) {
                pictureList.add("http://img.my.csdn.net/uploads/201701/06/1483664940_9893.jpg");
            }
            topicResult.setPictureList(pictureList);
            mTopicList.add(topicResult);
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        mRecyclerView.setAdapter(new CommonRecyclerAdapter<TopicResult>(this, mTopicList, R.layout.recycler_main_message) {
            @Override
            protected void bindViewData(CommonViewHolder holder, TopicResult item) {
                PicturesLayout picturesLayout = (PicturesLayout) holder.getView(R.id.picture_layout);
                int childCount = picturesLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    LinearLayout linearLayout = (LinearLayout) picturesLayout.getChildAt(i);
                    int count = linearLayout.getChildCount();
                    Log.e(TAG, "bindViewData: linearLayout = " + linearLayout);
                    for (int k = 0; k < count; k++) {
                        Log.e(TAG, "bindViewData: view = " + linearLayout.getChildAt(k));
                    }

                }
                Log.e(TAG, "bindViewData:-------------------------------------");
                picturesLayout.setPictureUrlList(item.getPictureList());

            }
        });
    }


}
