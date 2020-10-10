package com.example.group_w01_07_3.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.group_w01_07_3.R;

public class AccountFragment extends Fragment {

//    private ImageView avatarImage;
    FragmentPagerAdapter adapterViewPager;


    //为了速度暂时把Viewpager adapter放在这里,请单独建个JAVA
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return EditProfileFragment.newInstance();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return OpenedCapsuleHistoryFragment.newInstance();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Profile";
                case 1:
                    return "Opened Capsule History";
            }
            return "something wrong";
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);


        //给viewpager添加adapter
        ViewPager vpPager = (ViewPager) root.findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
        vpPager.setAdapter(adapterViewPager);



//        Button editProfileButton = (Button) root.findViewById(R.id.button_edit_profile);
//        editProfileButton.setOnClickListener(this);
//
//        Button openedCapsuleHistoryButton = (Button) root.findViewById(R.id.button_opened_capsule_history);
//        openedCapsuleHistoryButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                replaceFragment(new History());
//            }
//        });

//        replaceFragment(new EditProfileFragment());

//        avatarImage = (ImageView) root.findViewById(R.id.img_account_avatar);
//
//        final Button signOutButton = (Button) root.findViewById(R.id.button_acct_sign_out);
//        signOutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(AccountFragment.this.getContext());
//                builder.setIcon(R.drawable.warning);
//                builder.setTitle("Warning");
//                builder.setMessage("Do you want to sign out?");
//                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        signOutButton.setEnabled(false);
//                        String token = UserUtil.getToken(AccountFragment.this.getContext());
//                        if (token.isEmpty()) {
//                            Log.d("SIGNOUT", "Error: no token");
//                            Toast.makeText(AccountFragment.this.getContext(), "Error: no token", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
//                            startActivity(intent);
//                            getActivity().finish();
//                        } else {
//                            HttpUtil.signOut(token, new okhttp3.Callback() {
//                                @Override
//                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                                    Log.d("SIGNOUT", "***** signOut onResponse *****");
//                                    String responseData = response.body().string();
//                                    Log.d("SIGNOUT", "signOut: " + responseData);
//                                    try {
//                                        JSONObject responseJSON = new JSONObject(responseData);
//                                        if (responseJSON.has("success")) {
//                                            String status = responseJSON.getString("success");
//                                            Log.d("SIGNOUT", "signOut success: " + status);
//                                            ((Activity) AccountFragment.this.getContext()).runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    UserUtil.clearToken(AccountFragment.this.getContext());
//                                                    Toast.makeText(AccountFragment.this.getContext(), "Sign out successfully", Toast.LENGTH_SHORT).show();
//                                                    Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
//                                                    startActivity(intent);
//                                                    getActivity().finish();
//                                                }
//                                            });
//                                        } else if (responseJSON.has("error")) {
//                                            String status = responseJSON.getString("success");
//                                            Log.d("SIGNOUT", "signOut error: " + status);
//                                            ((Activity) AccountFragment.this.getContext()).runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    UserUtil.clearToken(AccountFragment.this.getContext());
//                                                    Toast.makeText(AccountFragment.this.getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
//                                                    Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
//                                                    startActivity(intent);
//                                                    getActivity().finish();
//                                                }
//                                            });
//                                        } else {
//                                            Log.d("SIGNOUT", "signOut: Invalid form");
//                                            ((Activity) AccountFragment.this.getContext()).runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    UserUtil.clearToken(AccountFragment.this.getContext());
//                                                    Toast.makeText(AccountFragment.this.getContext(), "Invalid form", Toast.LENGTH_SHORT).show();
//                                                    Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
//                                                    startActivity(intent);
//                                                    getActivity().finish();
//                                                }
//                                            });
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                        }
//                        dialog.dismiss();
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                builder.show();
//            }
//        });

//        Button editProfileButton = (Button) root.findViewById(R.id.button_acct_edit_profile);
//        editProfileButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(AccountFragment.this.getContext(), EditProfile.class);
//                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(AccountFragment.this.getActivity()).toBundle());
//            }
//        });
//
//        Button openedCapsuleHistory = (Button) root.findViewById(R.id.button_acct_view_opened_capsule_history);
//        openedCapsuleHistory.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(AccountFragment.this.getContext(), History.class);
//                startActivity(intent);
//            }
//        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.button_edit_profile:
//                replaceFragment(new EditProfileFragment());
//                break;
//            case R.id.button_opened_capsule_history:
//                replaceFragment(new History());
//                break;
//            default:
//                break;
//        }
//    }
//
//    private void replaceFragment(Fragment fragment) {
//        FragmentManager fragmentManager = getParentFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.account_edit_change_opened_fragment, fragment);
//        transaction.commit();
//    }

}