package com.example.group_w01_07_3.features.onboarding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.group_w01_07_3.MainActivity;
import com.example.group_w01_07_3.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Tutorial was taken to build the Onboarding activity
 * The detail implementation and design were modified to suit our APP & implementation logic
 * & the Material Design Guideline
 * Anyway, the tutorials listed below must be credited
 * Tutorial: https://www.youtube.com/watch?v=pwcG6npiXyo&t=79s
 */
public class OnBoardingActivity extends AppCompatActivity {
    // App View
    private TabLayout tabLayout;
    private ViewPager onboardingPager;
    private OnboardingViewPagerAdapter onboardingViewPagerAdapter;
    private MaterialButton btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Check if onBoarding activity has been finished or not
        if (restorePrefData()) {
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }

        setContentView(R.layout.activity_on_boarding);

        initOnboardingPager();
        initStartBtn();

    }

    /**
     * Restore preference data that if onboarding activity has been finished or not
     *
     * @return boolean indicator if if onboarding activity has been finished or not
     */
    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isIntroOpnend", false);
        return isIntroActivityOpnendBefore;

    }

    /**
     * save preference data that if onboarding activity has been finished or not
     */
    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpnend", true);
        editor.commit();
    }

    /**
     * Initialise the onboarding view content
     */
    private void initOnboardingPager() {
        //initialize views
        tabLayout = findViewById(R.id.onboarding_dot);
        btnGetStarted = findViewById(R.id.onboarding_started);

        //crate list data for onboarding pager
        final List<OnboardingItem> onboardingItemList = new ArrayList<>();
        onboardingItemList.add(new OnboardingItem("Record Your Footprint",
                "Record your footprint as Geo-capsules anytime, on the spot, which is waiting to be discovered.", R.drawable.ic_onboarding_save));
        onboardingItemList.add(new OnboardingItem("Discover The Past Moments",
                "Discover nearby Geo-capsules from yourself and others. Smile for your past and others moments", R.drawable.ic_onboarding_discover));
        onboardingItemList.add(new OnboardingItem("Review The Discovered Moments",
                "Review your discovered Geo-capsules from Geo-capsules gallery", R.drawable.ic_onboarding_history));

        onboardingPager = findViewById(R.id.onboarding_Viewpager);
        onboardingViewPagerAdapter = new OnboardingViewPagerAdapter(this, onboardingItemList);
        onboardingPager.setAdapter(onboardingViewPagerAdapter);

        tabLayout.setupWithViewPager(onboardingPager);

        //Show "GET STARTED" Button on last page, else hide it
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == onboardingItemList.size() - 1) {
                    btnGetStarted.setVisibility(View.VISIBLE); //last page show get started button
                    btnGetStarted.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_to_down_fast));
                } else {
                    btnGetStarted.setVisibility(View.INVISIBLE); //last page show get started button
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * allow user to click start button and navigate to sign in page
     */
    private void initStartBtn() {
        //apply alert sound
        final MediaPlayer mediaPlayer = MediaPlayer.create(OnBoardingActivity.this, R.raw.hero);
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);

                mediaPlayer.start();

                startActivity(mainActivity);

                //record this onboarding activity has been finished once
                savePrefsData();
                finish();
            }
        });
    }

}