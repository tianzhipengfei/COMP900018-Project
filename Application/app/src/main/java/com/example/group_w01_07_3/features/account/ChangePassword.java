package com.example.group_w01_07_3.features.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.group_w01_07_3.MainActivity;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignUp;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.google.android.material.progressindicator.ProgressIndicator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

public class ChangePassword extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button confirmChange;

    private EditText oldPasswordET;
    private EditText newPasswordET;
    private EditText reNewPasswordET;

    private String oldPassword;
    private String newPassword;
    private String reNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mToolbar = findViewById(R.id.change_password_back_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Password");

        //navigate back to account page. 请自己根据activity life cycle来写返回功能
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        oldPasswordET = (EditText) findViewById(R.id.edittext_change_password_old);
        newPasswordET = (EditText) findViewById(R.id.edittext_change_password_new);
        reNewPasswordET = (EditText) findViewById(R.id.edittext_change_password_re_new);

        confirmChange = (Button) findViewById(R.id.change_password_confirm_button);
        confirmChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmChange.setEnabled(false);

                oldPassword = oldPasswordET.getText().toString();
                newPassword = newPasswordET.getText().toString();
                reNewPassword = reNewPasswordET.getText().toString();

//                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_confirm_change_password);
//                progress.show();
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                View pageLayout = findViewById(R.id.change_password_mega_layout);
//                View root = pageLayout.getRootView();
//                root.setBackgroundColor(ContextCompat.getColor(ChangePassword.this, R.color.colorGreyOut));
//
//                //Replace this part to login successful or fail, which hide progress bar and display message
//                //will change background to indicate the process
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Do something here
//                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_confirm_change_password);
//                        progress.hide();
//                        View pageLayout = findViewById(R.id.change_password_mega_layout);
//                        View root = pageLayout.getRootView();
//                        root.setBackgroundColor(ContextCompat.getColor(ChangePassword.this, R.color.colorResetWhite));
//                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//                        Toast.makeText(ChangePassword.this, "checked state and implement logic accordingly", Toast.LENGTH_SHORT).show();
//                    }
//                }, 3000);

                if (oldPassword.isEmpty()) {
                    Toast.makeText(ChangePassword.this, "Please enter current password", Toast.LENGTH_SHORT).show();
                    confirmChange.setEnabled(true);
                } else if (newPassword.isEmpty()) {
                    Toast.makeText(ChangePassword.this, "Please enter new password", Toast.LENGTH_SHORT).show();
                    confirmChange.setEnabled(true);
                } else if (reNewPassword.isEmpty()) {
                    Toast.makeText(ChangePassword.this, "Please repeat new password", Toast.LENGTH_SHORT).show();
                    confirmChange.setEnabled(true);
                } else if (!isValidPassword(oldPassword)) {
                    Toast.makeText(ChangePassword.this, "check your current password", Toast.LENGTH_SHORT).show();
                    oldPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else if (!isValidPassword(newPassword)) {
                    Toast.makeText(ChangePassword.this, "invalid new password", Toast.LENGTH_SHORT).show();
                    newPasswordET.setText("");
                    reNewPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else if (!newPassword.equalsIgnoreCase(reNewPassword)) {
                    Toast.makeText(ChangePassword.this, "repeat new password doesn't match new password", Toast.LENGTH_SHORT).show();
                    reNewPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else if (oldPassword.equalsIgnoreCase(newPassword)) {
                    Toast.makeText(ChangePassword.this, "new password is the same as old password", Toast.LENGTH_SHORT).show();
                    oldPasswordET.setText("");
                    newPasswordET.setText("");
                    reNewPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else {
                    onChangePassword();
                }
            }
        });

    }

    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[\\.#?!@$%^&*-_=+]).{8,}$");
        Matcher m = p.matcher(password);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    private void onChangePassword() {
        if (UserUtil.getToken(ChangePassword.this).isEmpty()) {
            Toast.makeText(ChangePassword.this, "no token", Toast.LENGTH_SHORT).show();
        } else {
            HttpUtil.changePassword(UserUtil.getToken(ChangePassword.this), oldPassword, newPassword, new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d("PASS", "***** changePassword onResponse *****");
                    String responseData = response.body().string();
                    Log.d("PASS", "changePassword: " + responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.d("PASS", "changePassword success: " + status);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ChangePassword.this, "Change password successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        } else if (responseJSON.has("error")) {
                            String status = responseJSON.getString("error");
                            Log.d("PASS", "changePassword error: " + status);
                            if (status.equalsIgnoreCase("invalidPass - invalid password, try again")) {
                                runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      Toast.makeText(ChangePassword.this, "Wrong old password", Toast.LENGTH_SHORT).show();
                                                      oldPasswordET.setText("");
                                                      confirmChange.setEnabled(true);
                                                  }
                                              }
                                );
                            } else if (status.equalsIgnoreCase("Not logged in")) {
                                Log.d("PASS", "changePassword error: " + status);
                                runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      Toast.makeText(ChangePassword.this, "Not logged in", Toast.LENGTH_SHORT).show();
                                                      Intent intent = new Intent(ChangePassword.this, DiscoverCapsule.class);
                                                      startActivity(intent);
                                                      finish();
                                                      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                      confirmChange.setEnabled(true);
                                                  }
                                              }
                                );
                            }
                        } else {
                            Log.d("PASS", "changePassword: Invalid form");
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  Toast.makeText(ChangePassword.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
                                                  confirmChange.setEnabled(true);
                                              }
                                          }
                            );
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
}