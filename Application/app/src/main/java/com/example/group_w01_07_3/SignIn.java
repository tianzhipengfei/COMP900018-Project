package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.CaesarCipherUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {
    // APP view
    private ConstraintLayout constraintLayout;
    private EditText usernameET;
    private EditText passwordET;
    private CheckBox rememberCB;
    private Button signInButton;

    // preference data
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // message section
    private Toast toast = null;
    private Snackbar snackbar = null;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        constraintLayout = findViewById(R.id.sign_in_mega_layout);

        //don't pop uo keyboard when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        pref = getSharedPreferences("remember", MODE_PRIVATE);
        usernameET = (EditText) findViewById(R.id.edittext_sign_in_username);
        passwordET = (EditText) findViewById(R.id.edittext_sign_in_password);
        rememberCB = (CheckBox) findViewById(R.id.checkbox_sign_in_remember);
        boolean isRemember = pref.getBoolean("remember", false);
        if (isRemember) {
            String username = CaesarCipherUtil.decrypt(pref.getString("username", ""), 9);
            String password = CaesarCipherUtil.decrypt(pref.getString("password", ""), 4);
            usernameET.setText(username);
            passwordET.setText(password);
            rememberCB.setChecked(true);
        }

        signInButton = (Button) findViewById(R.id.button_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                signInButton.setEnabled(false);

                String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();

                if (username.isEmpty()) {
                    displayToast(SignIn.this, "Username is required", Toast.LENGTH_SHORT);
                    signInButton.setEnabled(true);
                } else if (password.isEmpty()) {
                    displayToast(SignIn.this, "Password is required", Toast.LENGTH_SHORT);
                    signInButton.setEnabled(true);
                } else {
                    boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                    if(!internetFlag){
                        displaySnackbar(constraintLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
                        signInButton.setEnabled(true);
                        return ;
                    }

                    editor = pref.edit();
                    if (rememberCB.isChecked()) {
                        editor.putBoolean("remember", true);
                        editor.putString("username", CaesarCipherUtil.encrypt(username, 9));
                        editor.putString("password", CaesarCipherUtil.encrypt(password, 4));
                    } else {
                        editor.clear();
                    }
                    editor.apply();

                    HttpUtil.signIn(username.toLowerCase(), password, new okhttp3.Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            Log.d("SIGNIN", "***** signIn onResponse *****");
                            String responseData = response.body().string();
                            Log.d("SIGNIN", "signIn: " + responseData);
                            try {
                                JSONObject responseJSON = new JSONObject(responseData);
                                if (responseJSON.has("success")) {
                                    String status = responseJSON.getString("success");
                                    Log.d("SIGNIN", "signIn success: " + status);
                                    // token
                                    String token = responseJSON.getString("token");
                                    Log.d("SIGNIN", "signIn token: " + token);
                                    UserUtil.setToken(SignIn.this, token);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            displayToast(SignIn.this, "Sign in successfully", Toast.LENGTH_SHORT);
                                            Intent intent = new Intent(SignIn.this, DiscoverCapsule.class);
                                            finish();
                                            startActivity(intent);
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }
                                    });
                                } else if (responseJSON.has("error")) {
                                    String status = responseJSON.getString("error");
                                    Log.d("SIGNIN", "signIn error: " + status);
                                    if (status.equalsIgnoreCase("userNotExist - user does not exist")) {
                                        runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              usernameET.setText("");
                                                              passwordET.setText("");
                                                              displayToast(SignIn.this, "userNotExist - user does not exist", Toast.LENGTH_SHORT);
                                                              signInButton.setEnabled(true);
                                                          }
                                                      }
                                        );
                                    } else if (status.equalsIgnoreCase("invalidPass - invalid password, try again")) {
                                        Log.d("SIGNIN", "signIn error: " + status);
                                        runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              passwordET.setText("");
                                                              displayToast(SignIn.this, "invalidPass - invalid password, try again", Toast.LENGTH_SHORT);
                                                              signInButton.setEnabled(true);
                                                          }
                                                      }
                                        );
                                    } else if (status.equalsIgnoreCase("loginError - cannot login")) {
                                        Log.d("SIGNIN", "signIn error: " + status);
                                        runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              displayToast(SignIn.this, "loginError - cannot login", Toast.LENGTH_SHORT);
                                                              signInButton.setEnabled(true);
                                                          }
                                                      }
                                        );
                                    }
                                } else {
                                    Log.d("SIGNIN", "signIn: Invalid form");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            displayToast(SignIn.this, "Invalid form, please try again later", Toast.LENGTH_SHORT);
                                            signInButton.setEnabled(true);
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displaySnackbar(constraintLayout, "Sign in timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
                                    signInButton.setEnabled(true);
                                }
                            });
                            return ;
                        }
                    });
                }
            }
        });

        TextView createAccountButton = (TextView) findViewById(R.id.text_create_account);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        ImageView sideAddShape = (ImageView) findViewById(R.id.sign_in_side_add);
        sideAddShape.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

    }

    /**
     * Display toast in a non-overlap manner
     *
     * @param context The context which toast will display at
     * @param msg     The message to display
     * @param length  the duration of toast display
     */
    private void displayToast(Context context, String msg, int length) {
        if (toast == null || !toast.getView().isShown()) {
            toast = Toast.makeText(context, msg, length);
            toast.show();
        }
    }

    /**
     * Display snackbar in a non-overlap manner
     *
     * @param view   view where snackbar will display at
     * @param msg    the message to display
     * @param length the duration of snackbar display
     */
    private void displaySnackbar(View view, String msg, int length) {
        if (snackbar == null || !snackbar.getView().isShown()) {
            snackbar = Snackbar.make(view, msg, length);
            snackbar.show();
        }
    }
}