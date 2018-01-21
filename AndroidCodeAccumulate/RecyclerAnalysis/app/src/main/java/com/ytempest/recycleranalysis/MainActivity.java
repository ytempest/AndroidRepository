package com.ytempest.recycleranalysis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ytempest.recycleranalysis.commonRecyclerUse.CommonRecyclerActivity;
import com.ytempest.recycleranalysis.division.DivisionUseActivity;

public class MainActivity extends AppCompatActivity {

    private Button mBaseUse;
    private Button mCommonUse;

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
    }

}
