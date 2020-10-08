package com.example.group_w01_07_3;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.group_w01_07_3.ui.account.AccountFragment;
import com.example.group_w01_07_3.ui.create.CreateCapsuleFragment;
import com.example.group_w01_07_3.ui.discover.DiscoverCapsuleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
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

//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);
//
//        NavigationUI.setupWithNavController(bottomNavigationView, navController);


        //hide bottom navigation view according to ID of layout
//        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
//            @Override
//            public void onDestinationChanged(@NonNull NavController controller,
//                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
//                BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
//
//                if(destination.getId() == R.id.editProfile) {
//                    bottomNavigationView.setVisibility(View.GONE);
//                } else {
//                    bottomNavigationView.setVisibility(View.VISIBLE);
//                }
//            }
//        });



//        //loading the default fragment
//        loadFragment(new DiscoverCapsuleFragment());
////
//        //getting bottom navigation view and attaching the listener
//        BottomNavigationView navigation = findViewById(R.id.nav_host_fragment);
//        navigation.setOnNavigationItemSelectedListener(this);

        //初始化Fragment及底部导航栏
        initFragment(savedInstanceState);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        //关闭底部导航栏默认动画效果并添加监听器
//        disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void initFragment(Bundle savedInstanceState) {
        //判断activity是否重建，如果不是，则不需要重新建立fragment.
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (accountFragment == null) {
                accountFragment = new AccountFragment();
            }
            isFragment = accountFragment;
            ft.replace(R.id.nav_host_fragment, accountFragment).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.accountFragment:
                    if (accountFragment == null) {
                        accountFragment = new AccountFragment();
                    }
                    switchContent(isFragment, accountFragment);
                    return true;
                case R.id.createCapsuleFragment:
                    if (createCapsuleFragment == null) {
                        createCapsuleFragment = new CreateCapsuleFragment();
                    }
                    switchContent(isFragment, createCapsuleFragment);
                    return true;
                case R.id.discoverCapsuleFragment:
                    if (discoverCapsuleFragment == null) {
                        discoverCapsuleFragment = new DiscoverCapsuleFragment();
                    }
                    switchContent(isFragment, discoverCapsuleFragment);
                    return true;
            }
            return false;
        }

    };

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
//        Fragment fragment = null;
//
//        switch (item.getItemId()) {
//            case R.id.discoverCapsuleFragment:
//                fragment = new DiscoverCapsuleFragment();
//                break;
//
//            case R.id.createCapsuleFragment:
//                fragment = new CreateCapsuleFragment();
//                break;
//
//            case R.id.accountFragment:
//                fragment = new AccountFragment();
//                break;
//        }
//
//        return loadFragment(fragment);
//    }
////
////
//    //help us to switch between fragments
//    private boolean loadFragment(Fragment fragment) {
//        //switching fragment
//        if (fragment != null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.nav_host_fragment, fragment)
//                    .commit();
//            return true;
//        }
//        return false;
//    }

}