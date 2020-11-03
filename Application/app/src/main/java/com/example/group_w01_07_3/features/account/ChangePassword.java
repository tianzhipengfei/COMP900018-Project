package com.example.group_w01_07_3.features.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignIn;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.MessageUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

public class ChangePassword extends AppCompatActivity {

    private long mLastClickTime = 0;

    private Toolbar mToolbar;
    private Button confirmChange;

    private EditText oldPasswordET;
    private EditText newPasswordET;
    private EditText reNewPasswordET;

    private String oldPassword;
    private String newPassword;
    private String reNewPassword;

    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);

        //don't pop uo keyboard when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        initView();
        setupChangePasswordBtn();

    }

    private void initView() {
        constraintLayout = findViewById(R.id.change_password_mega_layout);
        mToolbar = findViewById(R.id.change_password_back_toolbar);
        oldPasswordET = (EditText) findViewById(R.id.edittext_change_password_old);
        newPasswordET = (EditText) findViewById(R.id.edittext_change_password_new);
        reNewPasswordET = (EditText) findViewById(R.id.edittext_change_password_re_new);
        confirmChange = (Button) findViewById(R.id.change_password_confirm_button);

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Password");

        //navigate back to account page.
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView helpImageButton = (ImageView) findViewById(R.id.imageButton_sign_up_question);
        helpImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guideline = getString(R.string.registration_password);
                AlertDialog dialog = new AlertDialog.Builder(ChangePassword.this)
                        .setIcon(R.drawable.sign_up_rules)
                        .setTitle("Password Reset Guideline")
                        .setMessage(guideline)
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });

    }

    private void setupChangePasswordBtn() {
        confirmChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                confirmChange.setEnabled(false);

                oldPassword = oldPasswordET.getText().toString();
                newPassword = newPasswordET.getText().toString();
                reNewPassword = reNewPasswordET.getText().toString();

                //TODO: @CHENFU, can use switch tp handle this long if statement
                if (oldPassword.isEmpty()) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Please enter the current password.", Snackbar.LENGTH_SHORT);
                    confirmChange.setEnabled(true);
                } else if (newPassword.isEmpty()) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Please enter the new password.", Snackbar.LENGTH_SHORT);
                    confirmChange.setEnabled(true);
                } else if (reNewPassword.isEmpty()) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Please repeat the new password.", Snackbar.LENGTH_SHORT);
                    confirmChange.setEnabled(true);
                } else if (!isValidPassword(oldPassword)) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Incorrect current password.", Snackbar.LENGTH_SHORT);
                    oldPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else if (!isValidPassword(newPassword)) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Invalid new password.", Snackbar.LENGTH_SHORT);
                    newPasswordET.setText("");
                    reNewPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else if (!newPassword.equalsIgnoreCase(reNewPassword)) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Repeat new password doesn't match new password.", Snackbar.LENGTH_SHORT);
                    reNewPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else if (oldPassword.equalsIgnoreCase(newPassword)) {
                    MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Matching new password and current password.", Snackbar.LENGTH_SHORT);
                    oldPasswordET.setText("");
                    newPasswordET.setText("");
                    reNewPasswordET.setText("");
                    confirmChange.setEnabled(true);
                } else {
                    boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                    if (!internetFlag) {
                        MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Please check internet connection.", Snackbar.LENGTH_LONG);
                        confirmChange.setEnabled(true);
                        return;
                    }
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
            MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Corrupted user token, lease reinstall app", Snackbar.LENGTH_SHORT);
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
                                    MessageUtil.displayToast(ChangePassword.this, "Change password successfully", Toast.LENGTH_SHORT);
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                }
                            });
                        } else if (responseJSON.has("error")) {
                            String status = responseJSON.getString("error");
                            Log.d("PASS", "changePassword error: " + status);
                            if (status.equalsIgnoreCase("invalidPass - invalid password, try again")) {
                                runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      MessageUtil.displaySnackbar(constraintLayout, "Change password failed. Please input a valid password.", Snackbar.LENGTH_SHORT);
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
                                                      UserUtil.clearToken(ChangePassword.this);
                                                      Intent intent = new Intent(ChangePassword.this, SignIn.class);
                                                      startActivity(intent);
                                                      finish();
                                                      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                  }
                                              }
                                );
                            }
                        } else {
                            Log.d("PASS", "changePassword: Invalid form");
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  MessageUtil.displaySnackbar(constraintLayout, "Change password timeout. Invalid form.", Snackbar.LENGTH_SHORT);
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MessageUtil.displaySnackbar(constraintLayout, "Change password timeout. Please check Internet connection.", Snackbar.LENGTH_LONG);
                            confirmChange.setEnabled(true);
                        }
                    });
                    return;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}