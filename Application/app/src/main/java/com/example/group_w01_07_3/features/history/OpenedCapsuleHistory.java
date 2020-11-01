package com.example.group_w01_07_3.features.history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignIn;
import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import androidx.core.util.Pair;

public class OpenedCapsuleHistory extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, CapsuleCallback{

    private ShimmerFrameLayout mShimmerViewContainer;

    boolean doubleBackToExitPressedOnce = false;

    private DrawerLayout drawerLayout;
    View headerview;
    TextView headerUsername;
    ShapeableImageView headerAvatar;
    private String usernameProfileString, avatarProfileString;

    PullLoadMoreRecyclerView recyclerView;
    OpenedCapsuleAdapter openedCapsuleAdapter;

    NavigationView navigationView;

    private Toolbar mToolbar;

    private List<OpenedCapsule> testingList;

    private int recycleInt = 0; //TODO: 测试用的，记得删除

    private int records_num_pere_request = 5;

    //Placeholder View for Disconnection logic
    TextView placeholder_emptyHistoryText;
    ImageView placeholder_retryImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Define Transition, used specifically during shared element transition
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Fade fade = new Fade();
        window.setEnterTransition(fade);
        window.setExitTransition(fade);
        window.setAllowEnterTransitionOverlap(false);
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
        headerAvatar = headerview.findViewById(R.id.header_avatar);

        updateHeader();

        //TODO:Image load请一定一定要用,不要自己写function(不然没法做animation) : [Picasso] 或者 [Glide】. 非常简单,有URL他就帮你load,只要几行代码, 详情请谷歌
        //load everything needed to be displyaed in the list
        recyclerView = findViewById(R.id.history_opened_capsule_list);
        testingList = new ArrayList<OpenedCapsule>();

        //placeholder View
        placeholder_emptyHistoryText = findViewById(R.id.history_opened_capsule_no_history_text);
        placeholder_retryImage = findViewById(R.id.history_opened_capsule_plz_retry_img);

        //set up the recycle view
        openedCapsuleAdapter = new OpenedCapsuleAdapter(this, testingList, this);
        recyclerView.setAdapter(openedCapsuleAdapter);
        recyclerView.setLinearLayout();
        recyclerView.setPullRefreshEnable(false);
        recyclerView.setFooterViewText("Loading More...Please Wait");

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        testingList.clear();

        this.onGetHistory();


        recyclerView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                onGetHistory();
            }
        });

    }

    private void onGetHistory() {
        HttpUtil.getHistory(UserUtil.getToken(OpenedCapsuleHistory.this), testingList.size(), records_num_pere_request, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("GET HISTORY", "***** GET HISTORY onResponse *****");
                String responseData = response.body().string();
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    if (responseJSON.has("success")) {
                        String status = responseJSON.getString("success");
                        Log.d("GET HISTORY", "GET HISTORY success: " + status);
                        JSONArray records = responseJSON.getJSONArray("hisotry");
                        // contains new records
                        if(records.length() != 0){
                            for (int i=0; i<records.length(); i++) {
                                JSONObject record = records.getJSONObject(i);
                                String capsule_title = record.getString("ctitle");
                                String opened_date = record.getString("htime");
                                String avatar_url = record.getString("cavatar");
                                String capsule_url = record.getString("cimage");
                                int tag = record.getInt("cpermission");
                                String content = record.getString("ccontent");
                                String username = record.getString("cusr");
                                String voice_url = record.getString("caudio");
                                testingList.add(new OpenedCapsule(capsule_title, opened_date, avatar_url, capsule_url, tag, content, username, voice_url));
                            }
                            OpenedCapsuleHistory.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                openedCapsuleAdapter.notifyDataSetChanged();
                                // stop animating Shimmer and hide the layout
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.INVISIBLE);
                                //必须要晚一点设置complete
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.setPullLoadMoreCompleted();
                                        return;
                                    }
                                },100);

                                }
                            });
                        }
                        else{
                            OpenedCapsuleHistory.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                // stop animating Shimmer and hide the layout
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.INVISIBLE);
                                Snackbar snackbar = Snackbar
                                        .make(drawerLayout, "No more records", 5000);
                                snackbar.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.setPullLoadMoreCompleted();
                                        recyclerView.setPushRefreshEnable(false);  //一定要用这个,不然会卡主
                                        return;
                                    }
                                },100);
                                }
                            });
                        }
                    } else if (responseJSON.has("error")) {
                        String status = responseJSON.getString("error");
                        Log.d("PROFILE", "getProfile error: " + status);
                        OpenedCapsuleHistory.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UserUtil.clearToken(OpenedCapsuleHistory.this);
                                Toast.makeText(OpenedCapsuleHistory.this, "Not logged in", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(OpenedCapsuleHistory.this, SignIn.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                            }
                        });
                    } else {
                        Log.d("GET HISTORY", "GET HISTORY: Invalid form");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                OpenedCapsuleHistory.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar
                                .make(drawerLayout, "Retrieve history timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.INVISIBLE);

                    }
                });
                return;
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

        //These three Top-level elements are added to transition to avoid blinking
        View statusBar = findViewById(android.R.id.statusBarBackground);
        View navigationBar = findViewById(android.R.id.navigationBarBackground);
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        Pair<View,String> p9 = Pair.create(statusBar,Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME); // second arg is the transition string Name
        Pair<View,String> p10 = Pair.create(navigationBar,Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME); // second arg is the transition string Name
        Pair<View,String> p11 = Pair.create((View)toolbar,"capsuleToolbarTN"); // second arg is the transition string Name

//        Log.d("this", String.valueOf(this == null));
//        Log.d("p1 == null", String.valueOf(p1 == null));
//        Log.d("p2 == null", String.valueOf(p2 == null));
//        Log.d("p3 == null", String.valueOf(p3 == null));
//        Log.d("p4 == null", String.valueOf(p4 == null));
//        Log.d("p5 == null", String.valueOf(p5 == null));
//        Log.d("p6 == null", String.valueOf(p6 == null));
//        Log.d("p7 == null", String.valueOf(p7 == null));
//        Log.d("p8 == null", String.valueOf(p8 == null));
//        Log.d("p9 == null", String.valueOf(p9 == null));
//        Log.d("p10 == null", String.valueOf(p10 == null));
//        Log.d("p11 == null", String.valueOf(p11 == null));

        //这里设置的就是到底哪几个view的transition被开启运作
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11);

//        Log.d("optionsCompat", String.valueOf(optionsCompat == null));

        // start the activity with scene transition

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.startActivity(OpenedCapsuleHistory.this, intent,optionsCompat.toBundle());
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

    private void updateHeader(){
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
                            avatarProfileString =  userInfoJSON.getString("uavatar");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!OpenedCapsuleHistory.this.isDestroyed()){
                                        headerUsername.setText(usernameProfileString);
                                        if (!(avatarProfileString == "null")){
                                            Picasso.with(OpenedCapsuleHistory.this)
                                                    .load(avatarProfileString)
                                                    .fit()
                                                    .placeholder(R.drawable.logo)
                                                    .into(headerAvatar);
                                        }
                                    } else {
                                        Log.d("FINISHED", "run: Activity has been finished, don't load Glide for update header avatar & username");
                                    }

                                }
                            });
                        } else if (responseJSON.has("error")) {
                            String status = responseJSON.getString("error");
                            Log.d("PROFILE", "getProfile error: " + status);
                            OpenedCapsuleHistory.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    UserUtil.clearToken(OpenedCapsuleHistory.this);
                                    Toast.makeText(OpenedCapsuleHistory.this, "Not logged in", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(OpenedCapsuleHistory.this, SignIn.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
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
                    //retry to update every 3 seconds. handle the case that enter the activity
                    //with no internet at all(which okHTTP will not retry for you)
                    if (!OpenedCapsuleHistory.this.isDestroyed()){
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateHeader();
                            }
                        },3000);
                    }
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