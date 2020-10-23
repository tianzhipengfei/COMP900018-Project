package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Tutorial was taken to build the Onboarding activity. The way to add dots is borrowed.
 * However, the implementation and design were modified to suit our APP, implementation logic
 * As well as the Material Design Guideline
 * Anyway, the tutorials listed below must be credited
 * Tutorial: https://www.youtube.com/watch?v=pwcG6npiXyo&t=79s
 */
public class OnBoardingActivity extends AppCompatActivity {

    private ViewPager onboardingPager;
    OnboardingViewPagerAdapter onboardingViewPagerAdapter;

    TabLayout tabLayout;

    MaterialButton btnGetStarted;

    private LinearLayout dotLayout;
    private TextView[] dotIndicators;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Check if onboarding activity has been finished or or
        if (restorePrefData()) {
            Intent mainActivity = new Intent(getApplicationContext(),MainActivity.class );
            startActivity(mainActivity);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }

        setContentView(R.layout.activity_on_boarding);

        //initialize views
        tabLayout = findViewById(R.id.onboarding_dot);
        btnGetStarted = findViewById(R.id.onboarding_started);

        //crate list data for onboarding pager
        final List<OnboardingItem> onboardingItemList = new ArrayList<>();
        onboardingItemList.add(new OnboardingItem("Record Your Best Memory", "Record your best moments anytime, on the spot, waiting to be discovered.", R.drawable.ic_onboarding_save));
        onboardingItemList.add(new OnboardingItem("Discover The Past Moments", "Discover nearby past memory from yourself and others. Smile for your past and others moments", R.drawable.logo));
        onboardingItemList.add(new OnboardingItem("Review The Discovered Memory", "Review your opened memory capsules from digital memory collection", R.drawable.logo));

        onboardingPager = findViewById(R.id.onboarding_Viewpager);
        onboardingViewPagerAdapter = new OnboardingViewPagerAdapter(this, onboardingItemList);
        onboardingPager.setAdapter(onboardingViewPagerAdapter);

        tabLayout.setupWithViewPager(onboardingPager);

        //Show "GET STARTED" Button on last page, else hide it
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == onboardingItemList.size() - 1){
                    btnGetStarted.setVisibility(View.VISIBLE); //last page show get started button
                    btnGetStarted.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.top_to_down_fast));
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

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);

                //record this onboarding activity has been finished once
                savePrefsData();
                finish();
            }
        });
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        Boolean isIntroActivityOpnendBefore = pref.getBoolean("isIntroOpnend",false);
        return  isIntroActivityOpnendBefore;

    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpnend",true);
        editor.commit();
    }

}