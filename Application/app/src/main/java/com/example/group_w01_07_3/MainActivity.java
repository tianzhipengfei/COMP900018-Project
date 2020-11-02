package com.example.group_w01_07_3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.UserUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userToken = UserUtil.getToken(MainActivity.this);
        // if userToken exists, redirect already logged in user to Discover Geo-Capsule, else SignIn
        Intent intent;
        if (!userToken.isEmpty()) {
            intent = new Intent(MainActivity.this, DiscoverCapsule.class);
        } else {
            intent = new Intent(MainActivity.this, SignIn.class);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}