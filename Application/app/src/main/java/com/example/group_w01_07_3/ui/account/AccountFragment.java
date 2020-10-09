package com.example.group_w01_07_3.ui.account;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.group_w01_07_3.EditProfile;
import com.example.group_w01_07_3.History;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignIn;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class AccountFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        final Button signOutButton = (Button) root.findViewById(R.id.button_acct_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountFragment.this.getContext());
                builder.setIcon(R.drawable.warning);
                builder.setTitle("Warning");
                builder.setMessage("Do you want to sign out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOutButton.setEnabled(false);
                        String token = UserUtil.getToken(AccountFragment.this.getContext());
                        if (token.isEmpty()) {
                            Log.d("SIGNOUT", "Error: no token");
                            Toast.makeText(AccountFragment.this.getContext(), "Error: no token", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
                            startActivity(intent);
                        } else {
                            HttpUtil.signOut(token, new okhttp3.Callback() {
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    Log.d("SIGNOUT", "***** signOut onResponse *****");
                                    String responseData = response.body().string();
                                    Log.d("SIGNOUT", "signOut: " + responseData);
                                    try {
                                        JSONObject responseJSON = new JSONObject(responseData);
                                        if (responseJSON.has("success")) {
                                            String status = responseJSON.getString("success");
                                            Log.d("SIGNOUT", "signOut success: " + status);
                                            ((Activity) AccountFragment.this.getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UserUtil.clearToken(AccountFragment.this.getContext());
                                                    Toast.makeText(AccountFragment.this.getContext(), "Sign out successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
                                                    startActivity(intent);
                                                }
                                            });
                                        } else if (responseJSON.has("error")) {
                                            String status = responseJSON.getString("success");
                                            Log.d("SIGNOUT", "signOut error: " + status);
                                            ((Activity) AccountFragment.this.getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UserUtil.clearToken(AccountFragment.this.getContext());
                                                    Toast.makeText(AccountFragment.this.getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
                                                    startActivity(intent);
                                                }
                                            });
                                        } else {
                                            Log.d("SIGNOUT", "signOut: Invalid form");
                                            ((Activity) AccountFragment.this.getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UserUtil.clearToken(AccountFragment.this.getContext());
                                                    Toast.makeText(AccountFragment.this.getContext(), "Invalid form", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(AccountFragment.this.getContext(), SignIn.class);
                                                    startActivity(intent);
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
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        Button editProfileButton = (Button) root.findViewById(R.id.button_acct_edit_profile);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountFragment.this.getContext(), EditProfile.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(AccountFragment.this.getActivity()).toBundle());
            }
        });

        Button openedCapsuleHistory = (Button) root.findViewById(R.id.button_acct_view_opened_capsule_history);
        openedCapsuleHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountFragment.this.getContext(), History.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}