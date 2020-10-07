package com.example.group_w01_07_3;

import android.os.Bundle;
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
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);


        //hide bottom navigation view according to ID of layout
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

                if(destination.getId() == R.id.editProfileFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });



//        //loading the default fragment
//        loadFragment(new DiscoverCapsuleFragment());
//
//        //getting bottom navigation view and attaching the listener
//        BottomNavigationView navigation = findViewById(R.id.bottom_nav);
//        navigation.setOnNavigationItemSelectedListener(this);

    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        Fragment fragment = null;
//
//        switch (item.getItemId()) {
//            case R.id.navigation_discover:
//                fragment = new DiscoverCapsuleFragment();
//                break;
//
//            case R.id.navigation_capsule:
//                fragment = new CreateCapsuleFragment();
//                break;
//
//            case R.id.navigation_account:
//                fragment = new AccountFragment();
//                break;
//        }
//
//        return loadFragment(fragment);
//    }
//
//
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