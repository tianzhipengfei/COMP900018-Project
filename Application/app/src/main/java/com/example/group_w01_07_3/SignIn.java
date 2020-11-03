package com.example.group_w01_07_3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.CaesarCipherUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.MessageUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {

    // App view
    private ConstraintLayout constraintLayout;
    private EditText usernameET;
    private EditText passwordET;
    private CheckBox rememberCB;
    private Button signInButton;
    private TextView createAccountButton;
    private ImageView sideAddButton;

    // sharedPreference
    private SharedPreferences pref;

    // message section
    private long mLastClickTime = 0;

    // user info
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initView();
    }

    /**
     * initialize the view
     */
    private void initView() {
        constraintLayout = findViewById(R.id.sign_in_mega_layout);

        // don't pop up the keyboard when entering the screen
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        usernameET = (EditText) findViewById(R.id.edittext_sign_in_username);
        passwordET = (EditText) findViewById(R.id.edittext_sign_in_password);
        rememberCB = (CheckBox) findViewById(R.id.checkbox_sign_in_remember);

        restoreUserInfoOrNot();

        // sign in
        signInButton = (Button) findViewById(R.id.button_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // preventing multiple clicks, using a threshold of 1 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                signInButton.setEnabled(false);

                // get username and password
                username = usernameET.getText().toString();
                password = passwordET.getText().toString();

                if (username.isEmpty()) { // check username
                    MessageUtil.displayToast(SignIn.this, "Username is required", Toast.LENGTH_SHORT);
                    signInButton.setEnabled(true);
                } else if (password.isEmpty()) { // check password
                    MessageUtil.displayToast(SignIn.this, "Password is required", Toast.LENGTH_SHORT);
                    signInButton.setEnabled(true);
                } else {
                    // check Internet connection
                    boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                    if (!internetFlag) {
                        MessageUtil.displaySnackbar(constraintLayout, "Oops! Looks like you lost Internet connection.\n" +
                                "Please connect to the Internet and try again ...", Snackbar.LENGTH_LONG);
                        signInButton.setEnabled(true);
                        return;
                    }

                    // sign in logic
                    onSignIn();
                }
            }
        });

        initJumpToSignUpPage();
    }

    /**
     * remember username and password (restore or not)
     */
    private void restoreUserInfoOrNot() { // user info: username & password
        pref = getSharedPreferences("remember", MODE_PRIVATE);
        boolean isRemember = pref.getBoolean("remember", false);
        if (isRemember) {
            // remember username and password (decrypt username)
            String username = CaesarCipherUtil.decrypt(pref.getString("username", ""), 9);
            // remember username and password (decrypt password)
            String password = CaesarCipherUtil.decrypt(pref.getString("password", ""), 4);
            usernameET.setText(username);
            passwordET.setText(password);
            rememberCB.setChecked(true);
        }
    }

    /**
     * sign in logic
     */
    private void onSignIn() {
        createAccountButton.setEnabled(false);
        sideAddButton.setEnabled(false);
        HttpUtil.signIn(username.toLowerCase(), password, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    if (responseJSON.has("success")) { // success
                        storeUserInfoOrNot();

                        String token = responseJSON.getString("token"); // token
                        UserUtil.setToken(SignIn.this, token); // store token
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MessageUtil.displayToast(SignIn.this, "Sign in successfully", Toast.LENGTH_SHORT);
                                Intent intent = new Intent(SignIn.this, DiscoverCapsule.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        });
                    } else if (responseJSON.has("error")) { // error
                        String status = responseJSON.getString("error");
                        if (status.equalsIgnoreCase("userNotExist - user does not exist")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usernameET.setText("");
                                    passwordET.setText("");
                                    MessageUtil.displayToast(SignIn.this, "User does not exist", Toast.LENGTH_SHORT);
                                    signInButton.setEnabled(true);
                                    createAccountButton.setEnabled(true);
                                    sideAddButton.setEnabled(true);
                                }
                            });
                        } else if (status.equalsIgnoreCase("invalidPass - invalid password, try again")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    passwordET.setText("");
                                    MessageUtil.displayToast(SignIn.this, "Wrong password", Toast.LENGTH_SHORT);
                                    signInButton.setEnabled(true);
                                    createAccountButton.setEnabled(true);
                                    sideAddButton.setEnabled(true);
                                }
                            });
                        } else if (status.equalsIgnoreCase("loginError - cannot login")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MessageUtil.displayToast(SignIn.this, "Cannot sign in\n" +
                                            "Please try again ...", Toast.LENGTH_SHORT);
                                    signInButton.setEnabled(true);
                                    createAccountButton.setEnabled(true);
                                    sideAddButton.setEnabled(true);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                // sign in timeout
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageUtil.displaySnackbar(constraintLayout, "Sign in timeout!\n" +
                                "Please check your Internet and try again ...", Snackbar.LENGTH_LONG);
                        signInButton.setEnabled(true);
                        createAccountButton.setEnabled(true);
                        sideAddButton.setEnabled(true);
                    }
                });
            }
        });
    }

    /**
     * remember username and password (store or not)
     */
    private void storeUserInfoOrNot() { // user info: username & password
        SharedPreferences.Editor editor = pref.edit();
        if (rememberCB.isChecked()) {
            editor.putBoolean("remember", true);
            // remember username and password (encrypt username)
            editor.putString("username", CaesarCipherUtil.encrypt(username, 9));
            // remember username and password (encrypt password)
            editor.putString("password", CaesarCipherUtil.encrypt(password, 4));
        } else {
            editor.clear();
        }
        editor.apply();
    }

    /**
     * jump to sign up page to create an account
     */
    private void initJumpToSignUpPage() {
        // jump to sign up page
        createAccountButton = (TextView) findViewById(R.id.text_create_account);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // jump to sign up page
        sideAddButton = (ImageView) findViewById(R.id.sign_in_side_add);
        sideAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}