package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class EditProfile extends AppCompatActivity {
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set transition
        // make sure to do this before setContentView or else the app will crash
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.setEnterTransition(new Slide());
        window.setExitTransition(new Slide());


        setContentView(R.layout.activity_edit_profile);

        mToolbar = findViewById(R.id.edit_profile_back_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //navigate back to account page. 请自己根据activity life cycle来写返回功能
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                onBackPressed();
            }
        });

        Button changePasswordBtn = (Button) findViewById(R.id.edit_profile_btn_change_password);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfile.this, ChangePassword.class);
                startActivity(intent);
            }
        });

        TextView username = (TextView) findViewById(R.id.edit_profile_username_display);
        username.setText("Seems like the username changed during onCreate");
        TextView email = (TextView) findViewById(R.id.edit_profile_email_display);
        email.setText("Seems like the email changed during onCreate");
        TextView DOB = (TextView) findViewById(R.id.edit_profile_dob_display);
        DOB.setText("Seems like the dob changed during onCreate");

    }

}