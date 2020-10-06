package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.progressindicator.ProgressIndicator;

public class ChangePassword extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mToolbar = findViewById(R.id.change_password_back_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //navigate back to account page. 请自己根据activity life cycle来写返回功能
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button confirmChange = (Button) findViewById(R.id.change_password_confirm_button);
        confirmChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_confirm_change_password);
                progress.show();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                View pageLayout = findViewById(R.id.change_password_mega_layout);
                View root = pageLayout.getRootView();
                root.setBackgroundColor(ContextCompat.getColor(ChangePassword.this, R.color.colorGreyOut));

                //Replace this part to login successful or fail, which hide progress bar and display message
                //will change background to indicate the process
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something here
                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_confirm_change_password);
                        progress.hide();
                        View pageLayout = findViewById(R.id.change_password_mega_layout);
                        View root = pageLayout.getRootView();
                        root.setBackgroundColor(ContextCompat.getColor(ChangePassword.this, R.color.colorResetWhite));
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Toast.makeText(ChangePassword.this, "checked state and implement logic accordingly", Toast.LENGTH_SHORT).show();
                    }
                }, 3000);

            }
        });

    }
}