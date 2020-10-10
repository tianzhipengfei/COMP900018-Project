package com.example.group_w01_07_3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.group_w01_07_3.ChangePassword;
import com.example.group_w01_07_3.EditProfile;
import com.example.group_w01_07_3.R;

import static android.content.ContentValues.TAG;

public class EditProfileFragment extends Fragment {

    Fragment childFragment;

    public static EditProfileFragment newInstance() {

        Bundle args = new Bundle();

        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);


//        Button changePasswordBtn = (Button) root.findViewById(R.id.edit_profile_btn_change_password);
//        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getContext(), ChangePassword.class);
//                startActivity(intent);
//            }
//        });

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

            //测试区 1
//        Fragment navHostFragment = getFragmentManager().getPrimaryNavigationFragment();
//        if (navHostFragment != null){
//            Log.d(TAG, "Found Nav Host Fragment");
//        }

//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.hide(navHostFragment);
//        fragmentTransaction.commit();


        //测试区 2
        //等同于上面的
//        Fragment navHostFragment = getChildFragmentManager().findFragmentById(R.id.edit_profile_replaceable_fragment);
//        if (navHostFragment != null){
//            Log.d(TAG, "Found Child Fragment");
//        }
//
//        //这里证明了navHostFragment就是现在显示的fragment
//        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
//        fragmentTransaction.hide(navHostFragment);
//        fragmentTransaction.commit();

        //测试区3
//        Fragment navHostFragment = getChildFragmentManager().getPrimaryNavigationFragment();
//        if (navHostFragment != null){
//            Log.d(TAG, "Found Nav Host Fragment");
//        }
//
//        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
//        fragmentTransaction.hide(navHostFragment);
//        fragmentTransaction.commit();
//
//        View childView = navHostFragment.getView();
//        if (childView !=null){
//            Log.d(TAG, "Found not null child view");
//        }


//        Button btn = navHostFragment.getView().findViewById(R.id.edit_profile_btn_change_password);
//                if (btn !=null){
//            Log.d(TAG, "Found not null btn");
//        }


        //有问题
//        Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(-1);
//        if (currentFragment != null){
//            Log.d(TAG, "Found Current Displayed Fragment");
//        }

        //一定要通过getChildFragmentManager（）来找到子fragment
//        childFragment = getChildFragmentManager().findFragmentById(R.id.edit_profile_replaceable_fragment);
//        if (childFragment != null){
//            Log.d(TAG, "Found Child Fragment");
//        }

        //请谷歌三种获取Navcontroller的方法
//        NavController navController = NavHostFragment.findNavController(childFragment);
//        if (navController != null){
//            Log.d(TAG, "Found nav Controller in Edit Profile Mega Page");
//        }




    }
}