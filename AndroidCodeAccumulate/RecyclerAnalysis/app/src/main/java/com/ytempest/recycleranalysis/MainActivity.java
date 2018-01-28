package com.ytempest.recycleranalysis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ytempest.recycleranalysis.commonRecyclerUse.CommonRecyclerActivity;
import com.ytempest.recycleranalysis.divisionUse.DivisionUseActivity;
import com.ytempest.recycleranalysis.headerAndFooter.HeaderFooterActivity;
import com.ytempest.recycleranalysis.itemDragUse.DragItemActivity;
import com.ytempest.recycleranalysis.loadRefresh.RefreshLoadActivity;

public class MainActivity extends AppCompatActivity {

    private Button mBaseUse;
    private Button mCommonUse;
    private Button mHeaderFooter;
    private Button mRefresh;
    private Button mDragItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBaseUse = findViewById(R.id.bt_base_use);
        mBaseUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DivisionUseActivity.class));
            }
        });

        mCommonUse = findViewById(R.id.bt_common_use);
        mCommonUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CommonRecyclerActivity.class));
            }
        });

        mHeaderFooter = findViewById(R.id.bt_header_footer);
        mHeaderFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HeaderFooterActivity.class));
            }
        });

        mRefresh = findViewById(R.id.bt_load_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RefreshLoadActivity.class));
            }
        });


        mDragItem = findViewById(R.id.bt_drag_item);
        mDragItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DragItemActivity.class));
            }
        });


    }

}
