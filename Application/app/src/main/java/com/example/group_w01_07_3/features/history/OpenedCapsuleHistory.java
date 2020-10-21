package com.example.group_w01_07_3.features.history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class OpenedCapsuleHistory extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, CapsuleCallback{

    boolean doubleBackToExitPressedOnce = false;

    SwipeRefreshLayout swipeRefreshLayout;

    private DrawerLayout drawerLayout;
    View headerview;
    TextView headerUsername;
    private String usernameProfileString;

    OpenedCapsuleAdapter openedCapsuleAdapter;

    NavigationView navigationView;

    private Toolbar mToolbar;

    private List<OpenedCapsule> testingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opened_capsule_history);



        //设置主Activity的toolbar, 以及初始化侧滑菜单栏
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("History of Opened Capsules");

        drawerLayout = findViewById(R.id.history_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //设置侧滑菜单栏
        navigationView = findViewById(R.id.nav_view_history);
        navigationView.getMenu().getItem(2).setChecked(true); //setChecked myself
        navigationView.setNavigationItemSelectedListener(this);

        //the logic to find the header, then update the username from server user profile
        headerview = navigationView.getHeaderView(0);
        headerUsername = headerview.findViewById(R.id.header_username);

        updateHeaderUsername();




        //setup recycleView with adapter
        //这里我是手动添加的几个样本数据供测试layout用,写代码时请删除
        RecyclerView recyclerView = findViewById(R.id.history_opened_capsule_list);
        testingList = new ArrayList<>();
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));
        testingList.add(new OpenedCapsule("testing input capsule title", "2020/12/31", R.drawable.avatar_sample, R.drawable.capsule));

        openedCapsuleAdapter = new OpenedCapsuleAdapter(this, testingList, this);
        recyclerView.setAdapter(openedCapsuleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.history_swipe_refresh_layout);

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW);//设置进度框颜色的切换
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh () {

                //TODO: @CHENFU: 自行实现获取最新history的功能
                testingList.clear();

                testingList.add(new OpenedCapsule("New one ADDED", "2015/12/31", R.drawable.avatar_sample, R.drawable.capsule));
                testingList.add(new OpenedCapsule("New one ADDED", "2015/12/31", R.drawable.avatar_sample, R.drawable.capsule));
                testingList.add(new OpenedCapsule("New one ADDED", "2015/12/31", R.drawable.avatar_sample, R.drawable.capsule));

                openedCapsuleAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);//取消进度框
                Toast.makeText(OpenedCapsuleHistory.this, "Refresh Successful", Toast.LENGTH_SHORT).show();
            }
        });



//        mToolbar = findViewById(R.id.capsule_history_back_toolbar);
//        setSupportActionBar(mToolbar);
//        mToolbar.setNavigationIcon(R.drawable.ic_back);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//
//        //navigate back to account page. 请自己根据activity life cycle来写返回功能
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    @Override
    public void onCapsuleItemClick(int pos, TextView title, TextView date) {
        // create intent and send book object to Details activity

        Intent intent = new Intent(this,DetailedCapsuleHistoryItem.class);
        intent.putExtra("capsuleObject",testingList.get(pos));

        // shared Animation setup
        // let's import the Pair class
        Pair<View,String> p1 = Pair.create((View)title,"capsuleTitleTN"); // second arg is the transition string Name
        Pair<View,String> p2 = Pair.create((View)date,"capsuleDateTN"); // second arg is the transition string Name


        //这里设置的就是到底哪几个view的transition被开启运作
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,p1,p2);

        // start the activity with scene transition

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent,optionsCompat.toBundle());
        }
        else
            startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.discover_capsule_tab:
                intent = new Intent(OpenedCapsuleHistory.this, DiscoverCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.create_capsule_tab:
                intent = new Intent(OpenedCapsuleHistory.this, CreateCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.capsule_history_tab:
                //main activity cannot start itself again
                return true;
            case R.id.edit_profile_tab:
                intent = new Intent(OpenedCapsuleHistory.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
        }


        return false;
    }

    private void updateHeaderUsername(){
        if(!UserUtil.getToken(OpenedCapsuleHistory.this).isEmpty()){
            HttpUtil.getProfile(UserUtil.getToken(OpenedCapsuleHistory.this), new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d("PROFILE", "***** getProfile onResponse *****");
                    String responseData = response.body().string();
                    Log.d("PROFILE", "getProfile: " + responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.d("PROFILE", "getProfile success: " + status);
                            String userInfo = responseJSON.getString("userInfo");
                            JSONObject userInfoJSON = new JSONObject(userInfo);
                            usernameProfileString = userInfoJSON.getString("uusr");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    headerUsername.setText(usernameProfileString);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    //double backpressed to exit app
    //The logic is borrowed from https://stackoverflow.com/questions/8430805/clicking-the-back-button-twice-to-exit-an-activity
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawer(navigationView);
        }else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }
}