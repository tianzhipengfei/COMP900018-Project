package com.example.group_w01_07_3;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.group_w01_07_3.ui.account.AccountFragment;
import com.example.group_w01_07_3.ui.create.CreateCapsuleFragment;
import com.example.group_w01_07_3.ui.discover.DiscoverCapsuleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
//    public NavController navController;

//    private BottomNavigationView bottomNavigationView;
    //定义Fragment
    private AccountFragment accountFragment;
    private CreateCapsuleFragment createCapsuleFragment;
    private DiscoverCapsuleFragment discoverCapsuleFragment;
    //记录当前正在使用的fragment
    private Fragment isFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        //handle the humberger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //老版手动跳转
        //初始化Fragment及底部导航栏
        initFragment(savedInstanceState);
        NavigationView navigationView = findViewById(R.id.nav_view);
        //关闭底部导航栏默认动画效果并添加监听器
//        disableShiftMode(bottomNavigationView);
        navigationView.setNavigationItemSelectedListener(this);


//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
//        NavigationUI.setupWithNavController(navigationView, navController);
//        navigationView.setNavigationItemSelectedListener(this);

    }

    public void initFragment(Bundle savedInstanceState) {
        //判断activity是否重建，如果不是，则不需要重新建立fragment.
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (discoverCapsuleFragment == null) {
                discoverCapsuleFragment = new DiscoverCapsuleFragment();
            }
            isFragment = discoverCapsuleFragment;
            ft.replace(R.id.nav_host_fragment, discoverCapsuleFragment).commit();
        }
    }

        @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);

        drawerLayout.closeDrawers();
        int id = item.getItemId();

            switch (id) {
                case R.id.account:
                    if (accountFragment == null) {
                        accountFragment = new AccountFragment();
                    }
                    switchContent(isFragment, accountFragment);
                    return true;
                case R.id.capsule:
                    if (createCapsuleFragment == null) {
                        createCapsuleFragment = new CreateCapsuleFragment();
                    }
                    switchContent(isFragment, createCapsuleFragment);
                    return true;
                case R.id.discover:
                    if (discoverCapsuleFragment == null) {
                        discoverCapsuleFragment = new DiscoverCapsuleFragment();
                    }
                    switchContent(isFragment, discoverCapsuleFragment);
                    return true;
            }
        return true;
    }


    public void switchContent(Fragment from, Fragment to) {
        if (isFragment != to) {
            isFragment = to;
            FragmentManager fm = getSupportFragmentManager();
            //添加渐隐渐现的动画
            FragmentTransaction ft = fm.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                ft.hide(from).add(R.id.nav_host_fragment, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                ft.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }



//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        item.setChecked(true);
//
//        drawerLayout.closeDrawers();
//        int id = item.getItemId();
//
//        switch (id) {
//
//            case R.id.discover:
//                navController.navigate(R.id.discoverCapsuleFragment);
//                break;
//
//            case R.id.capsule:
//                navController.navigate(R.id.createCapsuleFragment);
//                break;
//
//            case R.id.account:
//                navController.navigate(R.id.accountFragment);
//                break;
//
//        }
//        return true;
//    }

    //if we open navigation drawer in activity, when we press back button, we dont want to leave
    //activity immediately, but close the navigation drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}