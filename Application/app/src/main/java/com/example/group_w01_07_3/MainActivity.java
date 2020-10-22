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

    private  static int SPLASH_TIME_OUT = 2000;

    Animation logoAnim, textAnim;

    ImageView logo;
    TextView title, subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


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


//        /**
//         * If token exists, jump to HomeActivity, else SignIn
//         */
//        if (UserUtil.getToken(MainActivity.this).isEmpty()) {
//            Intent intent = new Intent(MainActivity.this, SignIn.class);
//            startActivity(intent);
//            //kill MainActivity
//            finish();
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//        } else {
//            Intent intent = new Intent(MainActivity.this, DiscoverCapsule.class);
//            startActivity(intent);
//            //kill MainActivity
//            finish();
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//        }


//        //load animations
//        logoAnim = AnimationUtils.loadAnimation(this, R.anim.top_to_down);
//        textAnim = AnimationUtils.loadAnimation(this, R.anim.down_to_top);
//
//        //hooks
//        logo = findViewById(R.id.launch_logo);
//        title = findViewById(R.id.launch_title);
//        subtitle = findViewById(R.id.launch_subtitle);
//
//        logo.setAnimation(logoAnim);
//        title.setAnimation(textAnim);
//        subtitle.setAnimation(textAnim);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                /*
//                For the final version, app will follow three steps on launch
//                Step0: Launch Screen (A.K.A: Splash Screen)
//                Step1: Paper OnBoarding Screen(Slider picture feature introduction): first time open APP only
//                Step2: Login(Authentication successfully)
//                Step3: Discover Page(Homepage)
//                */
//
//                /**
//                 * If token exists, jump to HomeActivity, else SignIn
//                 */
//                if (UserUtil.getToken(MainActivity.this).isEmpty()) {
//                    Intent intent = new Intent(MainActivity.this, SignIn.class);
//                    startActivity(intent);
//                    //kill MainActivity
//                    finish();
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                } else {
//                    Intent intent = new Intent(MainActivity.this, DiscoverCapsule.class);
//                    startActivity(intent);
//                    //kill MainActivity
//                    finish();
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                }
//            }
//        },SPLASH_TIME_OUT);
        

    }
}