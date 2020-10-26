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
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

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

    private ShimmerFrameLayout mShimmerViewContainer;

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


        //Define Transition, used specifically during shared element transition
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.setEnterTransition(new Fade());
        window.setExitTransition(new Fade());
        window.setAllowReturnTransitionOverlap(false);

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



        //TODO: @CHENFu, 这里是我手动加的测试胶囊,请自行实现对应功能

        //TODO:Image load请一定一定要用,不要自己写function(不然没法做animation) : [Picasso] 或者 [Glide】. 非常简单,有URL他就帮你load,只要几行代码, 详情请谷歌
        //load everything needed to be displyaed in the list
        RecyclerView recyclerView = findViewById(R.id.history_opened_capsule_list);
        testingList = new ArrayList<>();
        final String testPurposeLongString = getApplicationContext().getString(R.string.registration_help);


        //set up the recycle view
        openedCapsuleAdapter = new OpenedCapsuleAdapter(this, testingList, this);
        recyclerView.setAdapter(openedCapsuleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        //TODO: @CHENFU: fetch capsule data into the Arraylist。 在data load的那一刻吧shimmerlayout给干掉,然后显示真正的data
        //TODO: 这里只是假设fetch capsule data用时为3s,请自己写真正的implemenmtation的函数
        //todo: Shimmerlayout教程链接 https://www.androidhive.info/2018/01/android-content-placeholder-animation-like-facebook-using-shimmer/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //假设终于data完全download好了
                testingList.clear();
                testingList.add(new OpenedCapsule("This is a very long title,This is a very long title,This is a very long title" +
                        "his is a very long title,This is a very long title,This is a very long title", "2019/12/31", R.drawable.avatar_sample, R.drawable.capsule, "Your Private Capsule",testPurposeLongString,"wcs123455"));
                testingList.add(new OpenedCapsule("testing input capsule title: aa", "2018/2/31", R.drawable.slidewindow_capsule, R.drawable.logo,"Public Memory Capsule",testPurposeLongString,"abfsdfb"));
                testingList.add(new OpenedCapsule("testing input capsule title: bb", "2017/3/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule","xcvxcvxcvxcv","wcs123455"));
                testingList.add(new OpenedCapsule("testing input capsule title: cc", "2016/4/31", R.drawable.avatar_sample, R.drawable.capsule,"Public Memory Capsule",testPurposeLongString,"wcs123455"));
                testingList.add(new OpenedCapsule("testing input capsule title: dd", "2015/5/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule","xcvxcvxcvxvxv","wcs123455"));
                testingList.add(new OpenedCapsule("testing input capsule title: ee", "2014/6/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule",testPurposeLongString,"wcs123455"));
                testingList.add(new OpenedCapsule("testing input capsule title: ff", "2020/7/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule","xcvxcvxcvxcxvcvcvxvxc","wcs123455"));
                testingList.add(new OpenedCapsule("testing input capsule title: gg", "2020/8/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule",testPurposeLongString,"wcs123455"));
                openedCapsuleAdapter.notifyDataSetChanged();

                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.INVISIBLE);

            }
        }, 3000);

        //TODO:@CHENFU, 这一块是负责下拉刷新列表的功能，请自行实现功能
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.history_swipe_refresh_layout);

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW);//设置进度框颜色的切换
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh () {

                //TODO: @CHENFU: 自行实现获取最新history的功能

                //Step1: when starting fetch data
                //set shimmer layout back, clear current recycleview
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmer();
                testingList.clear();
                openedCapsuleAdapter.notifyDataSetChanged();

                //step2: fetch data complete. set shimmer invisible, notify data change
                //假设终于把刷新的data下载下来花了3s
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        testingList.clear();

                        testingList.add(new OpenedCapsule("New one ADDED: 1st", "2016/12/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule","sfgdfsgsdfsdfgsdfgsdfg","wcs123455"));
                        testingList.add(new OpenedCapsule("New one ADDED: 2nd", "2017/12/31", R.drawable.avatar_sample, R.drawable.capsule,"Public Memory Capsule","sdfgsdfgdsfgfsdgdsgdsfgs","wcs123455"));
                        testingList.add(new OpenedCapsule("New one ADDED: 3rd", "2018/12/31", R.drawable.avatar_sample, R.drawable.capsule,"Your Private Capsule","sdfgsdgfsdgsdfgsdgsdfgds","wcs123455"));

                        openedCapsuleAdapter.notifyDataSetChanged();

                        // stop animating Shimmer and hide the layout
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);

                        swipeRefreshLayout.setRefreshing(false);//取消进度框

                        View bigView = findViewById(R.id.history_drawer_layout);
                        Snackbar snackbar = Snackbar.make(bigView, "Refreshed History", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                },3000);

            }
        });

    }

    @Override
    public void onCapsuleItemClick(int pos, TextView title, TextView date, ImageView capImage, TextView privateTag, TextView content, ImageView avatar, TextView by, TextView username) {
        // create intent and send book object to Details activity

        Intent intent = new Intent(this,DetailedCapsuleHistoryItem.class);
        intent.putExtra("capsuleObject",testingList.get(pos));

        // shared Animation setup
        // let's import the Pair class
        Pair<View,String> p1 = Pair.create((View)title,"capsuleTitleTN"); // second arg is the transition string Name
        Pair<View,String> p2 = Pair.create((View)date,"capsuleDateTN"); // second arg is the transition string Name
        Pair<View,String> p3 = Pair.create((View)capImage,"capsuleImageTN"); // second arg is the transition string Name
        Pair<View,String> p4 = Pair.create((View)privateTag,"capsuleTagTN"); // second arg is the transition string Name
        Pair<View,String> p5 = Pair.create((View)content,"capsuleContentTN"); // second arg is the transition string Name
        Pair<View,String> p6 = Pair.create((View)avatar,"capsuleAvatarTN"); // second arg is the transition string Name
        Pair<View,String> p7 = Pair.create((View)by,"capsuleByTN"); // second arg is the transition string Name
        Pair<View,String> p8 = Pair.create((View)username,"capsuleUsernameTN"); // second arg is the transition string Name

        //这里设置的就是到底哪几个view的transition被开启运作
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,p1,p2,p3,p4,p5,p6,p7,p8);

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