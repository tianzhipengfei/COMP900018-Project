package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

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

        // jump to Welcome On start
        Intent intent = new Intent(MainActivity.this, Welcome.class);
        startActivity(intent);
    }
}