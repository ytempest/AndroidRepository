package com.ytempest.aspectjdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ytempest.aspectjdemo.net.CheckNetAspect;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @CheckNetAspect
    public void onClick(View view) {
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
    }
}
