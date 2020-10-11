package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group_w01_07_3.ui.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.CaesarCipherUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.UserUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText usernameET;
    private EditText passwordET;
    private CheckBox rememberCB;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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

        //先暂时不做这个 sendUsernameToEmailButton

        signInButton = (Button) findViewById(R.id.button_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signin);
//                progress.show();
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                View pageLayout = findViewById(R.id.sign_in_mega_layout);
//                View root = pageLayout.getRootView();
//                root.setBackgroundColor(ContextCompat.getColor(SignIn.this, R.color.colorGreyOut));
//
//                //Replace this part to login successful or fail, which hide progress bar and display message
//                //will change background to indicate the process
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Do something here
//                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signin);
//                        progress.hide();
//                        View pageLayout = findViewById(R.id.sign_in_mega_layout);
//                        View root = pageLayout.getRootView();
//                        root.setBackgroundColor(ContextCompat.getColor(SignIn.this, R.color.colorResetWhite));
//                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//                        Toast.makeText(SignIn.this, "checked state and implement logic accordingly", Toast.LENGTH_SHORT).show();
//                    }
//                }, 3000);

                signInButton.setEnabled(false);

                String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(SignIn.this, "Username is required", Toast.LENGTH_SHORT).show();
                    signInButton.setEnabled(true);
                } else if (password.isEmpty()) {
                    Toast.makeText(SignIn.this, "Password is required", Toast.LENGTH_SHORT).show();
                    signInButton.setEnabled(true);
                } else {
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
                                            Toast.makeText(SignIn.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignIn.this, HomeActivity.class);
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
                                                              Toast.makeText(SignIn.this, "userNotExist - user does not exist", Toast.LENGTH_SHORT).show();
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
                                                              Toast.makeText(SignIn.this, "invalidPass - invalid password, try again", Toast.LENGTH_SHORT).show();
                                                              signInButton.setEnabled(true);
                                                          }
                                                      }
                                        );
                                    } else if (status.equalsIgnoreCase("loginError - cannot login")) {
                                        Log.d("SIGNIN", "signIn error: " + status);
                                        runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              Toast.makeText(SignIn.this, "loginError - cannot login", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(SignIn.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
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
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }
        });

        TextView skipToAccountButton = (TextView) findViewById(R.id.text_skip_to_account);
        skipToAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, DiscoverCapsule.class);
                //kill SinIn Activity
                //不用再kIll了,现在sign in是singleInstance(manifest-->launchmode--》singleInstance)
                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ImageView sideAddShape = (ImageView) findViewById(R.id.sign_in_side_add);
        sideAddShape.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }
        });

    }
}