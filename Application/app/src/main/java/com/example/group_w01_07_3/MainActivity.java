package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.UserUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_main);

    }
}