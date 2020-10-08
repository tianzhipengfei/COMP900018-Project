package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.group_w01_07_3.util.CaesarCipherUtil;

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

                String username = CaesarCipherUtil.encrypt((String) usernameET.getText().toString(), 9);
                String password = CaesarCipherUtil.encrypt((String) passwordET.getText().toString(), 4);

                editor = pref.edit();
                if (rememberCB.isChecked()) {
                    editor.putBoolean("remember", true);
                    editor.putString("username", username);
                    editor.putString("password", password);
                } else {
                    editor.clear();
                }
                editor.apply();
            }
        });

        TextView createAccountButton = (TextView) findViewById(R.id.text_create_account);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        TextView skipToAccountButton = (TextView) findViewById(R.id.text_skip_to_account);
        skipToAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, HomeActivity.class);
                //kill SinIn Activity
//                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

    }
}