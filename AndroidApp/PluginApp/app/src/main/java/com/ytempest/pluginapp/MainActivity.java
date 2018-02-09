package com.ytempest.pluginapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String user_name = intent.getStringExtra("user_name");
        Toast.makeText(this, "user_name=" + user_name, Toast.LENGTH_SHORT).show();
    }
}
