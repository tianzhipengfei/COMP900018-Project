package com.example.group_w01_07_3;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.group_w01_07_3.ui.account.EditProfileFragment;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountMegaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountMegaFragment extends Fragment {

    public NavController navController;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountMegaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountMegaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountMegaFragment newInstance(String param1, String param2) {
        AccountMegaFragment fragment = new AccountMegaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_mega, container, false);


        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //find navController
        navController = Navigation.findNavController(view);
        if(navController != null){
            Log.d(TAG, "found navController");
        }

        EditProfileFragment currentFragment = (EditProfileFragment)getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.vpPager + ":0");
        if (currentFragment != null){
            Log.d(TAG, "found EditProfileFragment");
        }

////                //NOTE!!!!!!!!!!CHANGE THIS,,,,,FORE TESTING PURPOSE, I CREATE A NEW FRAGMENT EVERY TIME. PLZ UPDATE TO perfect handle(check if exist)
////                ChangePasswordFragment newFragment = new ChangePasswordFragment();
////                //网上找的.Viewpager 里面getItem是根据这个公式来自动生成tag的
////                EditProfileFragment currentFragment = (EditProfileFragment)getFragmentManager().findFragmentByTag("android:switcher:" + R.id.vpPager + ":0");
////
////                FragmentManager manager = getParentFragmentManager();
////                FragmentTransaction transaction = manager.beginTransaction();
////
////                if (!newFragment.isAdded()) {    // 先判断是否被add过
//////                    View currentView = currentFragment.getView();
//////                    FrameLayout frameLayout = (FrameLayout) currentView.findViewById(R.id.edit_profile_fragment_mega_layout) ;
//////                    frameLayout.setVisibility(View.INVISIBLE);
////                    transaction
////                            .hide(currentFragment)
////                            .add(R.id.edit_profile_fragment_mega_layout, newFragment,"change_password_fragment")
////                            .commit(); // 隐藏当前的fragment，add下一个到Activity中
////                }
////                else {
////                    transaction.hide(currentFragment).show(newFragment).commit(); // 隐藏当前的fragment，显示下一个
////                }
////
////                transaction.add(R.id.edit_profile_fragment_mega_layout, newFragment,"changing_password");
////                transaction.addToBackStack(null);
////                transaction.commit();
        Button button = view.findViewById(R.id.edit_profile_btn_change_password);
        if(button != null){
            Log.d(TAG, "found change password button");
        }


    }
}