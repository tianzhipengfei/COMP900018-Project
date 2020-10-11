package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.UserUtil;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
        For the final version, app will follow three steps on launch
        Step1: Paper OnBoarding Screen(Slider picture feature introduction)
        Step2: Login(Authentication successfully)
        Step3: Discover Page(Homepage)
        */

        /**
         * If token exists, jump to HomeActivity, else SignIn
         */
        if (UserUtil.getToken(MainActivity.this).isEmpty()) {
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivity(intent);
            //kill MainActivity
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            Intent intent = new Intent(MainActivity.this, DiscoverCapsule.class);
            startActivity(intent);
            //kill MainActivity
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}