package com.example.group_w01_07_3.features.history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import org.jetbrains.annotations.NotNull;
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
        testingList = new ArrayList<>();
        final String testPurposeLongString = getApplicationContext().getString(R.string.registration_help);


        //set up the recycle view
        openedCapsuleAdapter = new OpenedCapsuleAdapter(this, testingList, this);
        recyclerView.setAdapter(openedCapsuleAdapter);
        recyclerView.setLinearLayout();
        recyclerView.setPullRefreshEnable(false);
        recyclerView.setFooterViewText("Loading More...Please Wait");

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        //TODO: @CHENFU: 完成第一次拉取历史胶囊数据,拉取3-5个。 在data load的那一刻把shimmerlayout stop+invisible,然后显示真正的data
        //TODO: 假设完成第一轮的下载花了3s， 这里只是假设fetch capsule data用时为3s,请自己写真正的implemenmtation first time fetch的函数
        //TODO: 假设第一次fectch得到的个数为 = [3]. 你跟ERIC协商一下具体个数。 我推荐为 --》 5 个/每次
        //TODO: Shimmerlayout教程链接 https://www.androidhive.info/2018/01/android-content-placeholder-animation-like-facebook-using-shimmer/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                testingList.clear();
                testingList.add(new OpenedCapsule("This is a very long title,This is a very long title,This is a very long title" +
                        "his is a very long title,This is a very long title,This is a very long title", "2019/12/31", "https://i.imgur.com/tGbaZCY.jpg", "https://i.imgur.com/tGbaZCY.jpg", 1,testPurposeLongString,"wcs123455","12345"));
                testingList.add(new OpenedCapsule("testing input capsule title: aa", "2018/2/31", "https://i.imgur.com/tGbaZCY.jpg", "https://i.imgur.com/tGbaZCY.jpg",0,testPurposeLongString,"abfsdfb","12345"));
                testingList.add(new OpenedCapsule("testing input capsule title: bb", "2017/3/31", "https://i.imgur.com/tGbaZCY.jpg", "https://i.imgur.com/tGbaZCY.jpg",1,"xcvxcvxcvxcv","wcs123455","12345"));
                testingList.add(new OpenedCapsule("testing input capsule title: bb", "2017/3/31", "https://i.imgur.com/tGbaZCY.jpg", "https://i.imgur.com/tGbaZCY.jpg",0,"xcvxcvxcvxcv","wcs123455","12345"));

                //假设终于data完全download好了
                openedCapsuleAdapter.notifyDataSetChanged();

                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.INVISIBLE);

            }
        }, 3000);

        recyclerView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
            }


            //TODO: @CHENFU 上拉加载更多功能实现. 在data拉取下来了后用handler delay方法setPullLoadMoreCompleted（），不然会卡主(library的原因)
            //TODO: @CHENFU 这里就不用管shimmer了
            //TODO: @CHENFU 请自行实现拉取功能,这里为测试用的秒加capsule
            @Override
            public void onLoadMore() {

                //TODO:逻辑: 每一轮都找server要 [5] 个胶囊。 然后load. 等完全load好了就notifyDataSetChanged()
                //TODO: 这样子recycleview就更新了数据(新的胶囊卡片就顺着你add的顺序, 加到列表的末尾了)
                //TODO: 最后把 recyclerView.setPullLoadMoreCompleted();来关闭底部显示的“loadingm ore, please wait”提示
                //TODO: 如果某一次server返回说没有更多的opened capsule了--》延时执行 setPullLoadMoreCompleted();-->setPushRefreshEnable(false)

                //TODO:这里我模拟了一下加了6轮数据(5次有数据,最后一次server提示没了)
                if (recycleInt == 0 || recycleInt == 2 || recycleInt == 4){
                    //假设这次花了2s
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            recyclerView.setPullLoadMoreCompleted();
                            recycleInt += 1;
//                            testingList.add(new OpenedCapsule("New one ADDED: 1st", "2016/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603620329.jpg", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700200.jpg",1,"sfgdfsgsdfsdfgsdfgsdfg","wcs123455","12345"));
//                            testingList.add(new OpenedCapsule("New one ADDED: 2nd", "2017/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700259.jpg", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700323.jpg",1,"sdfgsdfgdsfgfsdgdsgdsfgs","wcs123455","12345"));
//                            testingList.add(new OpenedCapsule("New one ADDED: 3rd", "2018/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700356.jpg", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700436.jpg",1,"sdfgsdgfsdgsdfgsdgsdfgds","wcs123455","12345"));
                            testingList.add(new OpenedCapsule("New one ADDED: 1st", "2016/12/31", "null", "null",1,"sfgdfsgsdfsdfgsdfgsdfg","wcs123455","null"));
                            testingList.add(new OpenedCapsule("New one ADDED: 2nd", "2017/12/31", "null", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700323.jpg",1,"sdfgsdfgdsfgfsdgdsgdsfgs","wcs123455","12345"));
                            testingList.add(new OpenedCapsule("New one ADDED: 3rd", "2018/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700356.jpg", "null",1,"sdfgsdgfsdgsdfgsdgsdfgds","wcs123455","12345"));
                            testingList.add(new OpenedCapsule("New one ADDED: 4th", "2018/12/31", "null", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603700436.jpg",1,"sdfgsdgfsdgsdfgsdgsdfgds","wcs123455","12345"));
                            openedCapsuleAdapter.notifyDataSetChanged();
                            Toast.makeText(OpenedCapsuleHistory.this, "first round refresh notified", Toast.LENGTH_SHORT).show();
                            //必须要晚一点设置complete
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.setPullLoadMoreCompleted();
                                    return;
                                }
                            },100);
                        }
                    },2000);

                }
                if(recycleInt == 1 || recycleInt == 3){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recycleInt += 1;
                            testingList.add(new OpenedCapsule("New one ADDED: 1st", "2016/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603620329.jpg", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603703210.jpg",1,"sfgdfsgsdfsdfgsdfgsdfg","wcs123455","12345"));
                            testingList.add(new OpenedCapsule("New one ADDED: 2nd", "2017/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603703077.jpg", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603703310.jpg",0,"sdfgsdfgdsfgfsdgdsgdsfgs","wcs123455","12345"));
                            testingList.add(new OpenedCapsule("New one ADDED: 3rd", "2018/12/31", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603703141.jpg", "https://www.tianzhipengfei.xin/static/mobile/wcs123455-1603703443.jpg",1,"sdfgsdgfsdgsdfgsdgsdfgds","wcs123455","12345"));
                            openedCapsuleAdapter.notifyDataSetChanged();
                            Toast.makeText(OpenedCapsuleHistory.this, "2nd round refresh notified", Toast.LENGTH_SHORT).show();
                            //必须要晚一点设置complete
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.setPullLoadMoreCompleted();
                                    return;
                                }
                            },100);

                        }
                    },2000);

                }

                if (recycleInt == 5){
                    //OK, 假设服务器返回说这是最后的capsule了，没有多的了, 然后把上拉添加功能给关闭
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //必须要晚一点设置complete
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.setPullLoadMoreCompleted();

                                    recyclerView.setPushRefreshEnable(false);  //一定要用这个,不然会卡主
                                    return;
                                }
                            },100);
                        }
                    },2000);

                }
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

        //这里设置的就是到底哪几个view的transition被开启运作
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11);

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
                                    headerUsername.setText(usernameProfileString);
                                    if (!(avatarProfileString == "null")){
                                        Picasso.with(OpenedCapsuleHistory.this)
                                                .load(avatarProfileString)
                                                .fit()
                                                .placeholder(R.drawable.logo)
                                                .into(headerAvatar);
                                    }

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