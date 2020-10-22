package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Two Tutorials were taken to build the Onboarding activity. The way to add dots is borrowed.
 * However, the implementation and design were modified to suit our APP, implementation logic
 * As well as the Material Design Guideline
 * Anyway, the tutorials listed below must be credited
 * Tutorial 1: https://www.youtube.com/watch?v=byLKoPgB7yA
 * Tutorial 2: https://www.youtube.com/watch?v=pwcG6npiXyo&t=79s
 */
public class OnBoardingActivity extends AppCompatActivity {

    private LinearLayout dotLayout;
    private TextView[] dotIndicators;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
//        startActivity(intent);
//        //kill MainActivity
//        finish();
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.activity_on_boarding);

        dotLayout = findViewById(R.id.onboarding_dot_layout);
        addDotsIndicator();
    }

    //
    private void addDotsIndicator(){
        dotIndicators = new TextView[3];

        for(int i=0; i < dotIndicators.length; i++){
            dotIndicators[i] = new TextView(this);
            dotIndicators[i].setText(Html.fromHtml("&#8226;"));
            dotIndicators[i].setTextSize(35);
            dotIndicators[i].setTextColor(getResources().getColor(R.color.colorPrimary));

            dotLayout.addView(dotIndicators[i]);
        }
    }
}