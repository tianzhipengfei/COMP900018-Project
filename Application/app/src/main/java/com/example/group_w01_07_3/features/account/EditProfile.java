package com.example.group_w01_07_3.features.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.google.android.material.navigation.NavigationView;

public class EditProfile extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener{
    private Toolbar mToolbar;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set transition
        // make sure to do this before setContentView or else the app will crash
//        Window window = getWindow();
//        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        window.setEnterTransition(new Slide());
//        window.setExitTransition(new Slide());
//
//
        setContentView(R.layout.activity_edit_profile);
//
//        mToolbar = findViewById(R.id.edit_profile_back_toolbar);
//        setSupportActionBar(mToolbar);
//        mToolbar.setNavigationIcon(R.drawable.ic_back);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//
//        //navigate back to account page. 请自己根据activity life cycle来写返回功能
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
////                onBackPressed();
//            }
//        });

        //设置主Activity的toolbar, 以及初始化侧滑菜单栏
        Toolbar toolbar = findViewById(R.id.toolbar_edit_profile);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Profile");

        drawerLayout = findViewById(R.id.edit_profile_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //设置侧滑菜单栏
        NavigationView navigationView = findViewById(R.id.nav_view_edit_profile);
        navigationView.getMenu().getItem(3).setChecked(true); //setChecked myself
        navigationView.setNavigationItemSelectedListener(this);





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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;

        //note. This is top level navigation(unrelated major task). Must use fade through
        switch (id){
            case R.id.discover_capsule_tab:
                intent = new Intent(EditProfile.this, DiscoverCapsule.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.create_capsule_tab:
                intent = new Intent(EditProfile.this, CreateCapsule.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.capsule_history_tab:
                intent = new Intent(EditProfile.this, OpenedCapsuleHistory.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.edit_profile_tab:
                //main activity cannot start itself again
                return true;
        }
        return false;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

}