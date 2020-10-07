package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.progressindicator.ProgressIndicator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

/*
1. dob不能是今天以后的日期
2. 3 <= username <= 20，只能由字母数字_组成，只能以字母开头，字母不区分大小写,不能重复
3. email满足基本格式
4. password至少8个字符，至少1个大写字母，1个小写字母，1个数字和1个特殊字符
5. 处理疯狂点击的问题
6. 处理没网的情况
*/

public class SignUp extends AppCompatActivity {

    private MaterialDatePicker<Long> picker;
    private Button dobPicker;
    private EditText usernameET;
    private EditText emailET;
    private EditText passwordET;
    private EditText reEnterPasswordET;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //implemented Material Date Picker, with constrained set with bound [1920/1/1, today]
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Please Select Your Birthday");

        Calendar cStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cStart.set(1920, 1, 1);

        Calendar cEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cEnd.set(cEnd.get(Calendar.YEAR), cEnd.get(Calendar.MONTH),cEnd.get(Calendar.DATE));
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        constraintsBuilder.setStart(cStart.getTimeInMillis());
        constraintsBuilder.setEnd(cEnd.getTimeInMillis());

        builder.setCalendarConstraints(constraintsBuilder.build());

        picker = builder.build();

        dobPicker = (Button) findViewById(R.id.button_sign_up_birthday);
        dobPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                dobPicker.setText(picker.getHeaderText());
            }
        });

        usernameET = (EditText) findViewById(R.id.edittext_sign_up_username);
        emailET = (EditText) findViewById(R.id.edittext_sign_up_email);
        passwordET = (EditText) findViewById(R.id.edittext_sign_up_password);
        reEnterPasswordET = (EditText) findViewById(R.id.edittext_sign_up_re_enter_password);

        signUpButton = (Button) findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpButton.setEnabled(false);
//                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signup);
//                progress.show();
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                View pageLayout = findViewById(R.id.sign_up_mega_layout);
//                View root = pageLayout.getRootView();
//                root.setBackgroundColor(ContextCompat.getColor(SignUp.this, R.color.colorGreyOut));
//
//                //Replace this part to login successful or fail, which hide progress bar and display message
//                //will change background to indicate the process
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Do something here
//                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signup);
//                        progress.hide();
//                        View pageLayout = findViewById(R.id.sign_up_mega_layout);
//                        View root = pageLayout.getRootView();
//                        root.setBackgroundColor(ContextCompat.getColor(SignUp.this, R.color.colorResetWhite));
//                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//                        Toast.makeText(SignUp.this, "checked state and implement logic accordingly", Toast.LENGTH_SHORT).show();
//                    }
//                }, 3000);

                String username = (String) usernameET.getText().toString().toLowerCase();
                Log.d("SIGNUP", "username: " + username);
                String email = (String) emailET.getText().toString();
                Log.d("SIGNUP", "email: " + email);
                String password = (String) passwordET.getText().toString();
                Log.d("SIGNUP", "password: " + password);
                String reEnterPassword = (String) reEnterPasswordET.getText().toString();
                Log.d("SIGNUP", "reEnterPassword: " + reEnterPassword);
                String dob = (String) dobPicker.getText();
                Log.d("SIGNUP", "dob: " + dob);

                if (allRequiredFinished(username, email, password, reEnterPassword, dob)){
                    HttpUtil.signUp(new String[] {username, password, email, dob, null}, new okhttp3.Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseData = response.body().string();
                            Log.d("SIGNUP", responseData);
                            try {
                                JSONObject responseJSON = new JSONObject(responseData);
                                if (responseJSON.has("success")) {
                                    String status = responseJSON.getString("success");
                                    Log.d("SIGNUP", status);
                                    runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          Toast.makeText(SignUp.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                                                          Intent intent = new Intent(SignUp.this, SignIn.class);
                                                          SignUp.super.finish();
                                                          startActivity(intent);
                                                          overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                      }
                                                  }
                                    );
                                } else if (responseJSON.has("error")) {
                                    String status = responseJSON.getString("error");
                                    Log.d("SIGNUP", status);
                                    runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          usernameET.setText("");
                                                          emailET.setText("");
                                                          Toast.makeText(SignUp.this, "Username or Email Address exists", Toast.LENGTH_SHORT).show();
                                                          signUpButton.setEnabled(true);
                                                      }
                                                  }
                                    );
                                } else {
                                    runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          Log.d("SIGNUP", "Invalid form");
                                                          Toast.makeText(SignUp.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
                                                          signUpButton.setEnabled(true);
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

                        }
                    });
                }

//                HttpUtil.uploadAvatar("t", new File("/data/data/com.example.group_w01_07_3/email.png"), new okhttp3.Callback() {
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                        Log.d("SIGNUP", "aaaaa");
//                        String r = response.body().string();
//                        Log.d("SIGNUP", r);
//                        try {
//                            String w = new JSONObject(r).getString("file");
//                            Log.d("SIGNUP", w);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        e.printStackTrace();
//                    }
//                });
            }
        });

        ImageView avatarImageBtn = (ImageView) findViewById(R.id.sign_up_avatar);
        avatarImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        TextView backToSignInText = (TextView) findViewById(R.id.text_back_sign_in);
        backToSignInText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                SignUp.super.finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private boolean allRequiredFinished(String username, String email, String password, String reEnterPassword, String dob) {
        if (username.isEmpty()) {
            Toast.makeText(SignUp.this, "Username is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (email.isEmpty()) {
            Toast.makeText(SignUp.this, "Email Address is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (password.isEmpty()) {
            Toast.makeText(SignUp.this, "Password is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (reEnterPassword.isEmpty()) {
            Toast.makeText(SignUp.this, "Re-enter Password is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (dob.equalsIgnoreCase("Select Birthday")) {
            Toast.makeText(SignUp.this, "Date of Birth is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        }

        if (!isValidUsername(username)) {
            signUpButton.setEnabled(true);
            return false;
        } else if (!isValidEmail(email)) {
            signUpButton.setEnabled(true);
            return false;
        } else if (!isValidPassword(password)) {
            signUpButton.setEnabled(true);
            return false;
        } else if (!password.equals(reEnterPassword)) {
            Toast.makeText(SignUp.this, "Re-enter Password does not match Password", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        }

        return true;
    }

    private boolean isValidUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            Toast.makeText(SignUp.this, "3 <= the length of Username <= 20", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern p = Pattern.compile("[a-z][0-9a-z_]{2,}");
        Matcher m = p.matcher(username);
        if (!m.matches()) {
            Toast.makeText(SignUp.this, "Username: alphanumeric_, starts with alpha, not case sensitive", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("\\w+@\\w+(\\.\\w+)+");
        Matcher m = p.matcher(email);
        if (!m.matches()) {
            Toast.makeText(SignUp.this, "Check your Email Address format", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[\\.#?!@$%^&*-]).{8,}$");
        Matcher m = p.matcher(password);
        if (!m.matches()) {
            Toast.makeText(SignUp.this, "Invalid Password", Toast.LENGTH_SHORT).show(); // remind user
            return false;
        }
        return true;
    }

}